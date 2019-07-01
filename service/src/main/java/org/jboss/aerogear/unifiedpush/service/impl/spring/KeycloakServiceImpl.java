package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.service.impl.UserTenantInfo;
import org.jboss.aerogear.unifiedpush.service.impl.spring.OAuth2Configuration.DomainMatcher;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@Service
public class KeycloakServiceImpl implements IKeycloakService {
    private static final Logger logger = LoggerFactory.getLogger(KeycloakServiceImpl.class);

	private static final String CLIENT_PREFIX = "ups-installation-";
	private static final String KEYCLOAK_ROLE_USER = "installation";
	private static final String UPDATE_PASSWORD_ACTION = "UPDATE_PASSWORD";

	private static final String ATTRIBUTE_VARIANT_SUFFIX = "_variantid";
	private static final String ATTRIBUTE_SECRET_SUFFIX = "_secret";
	public static final String USER_TENANT_RELATIONS = "userTenantRelations";

	private ConcurrentMap<String, RealmResource> realmNameToRealm = new ConcurrentHashMap<>();
    private volatile Boolean oauth2Enabled;
	private Keycloak kc;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private IOAuth2Configuration conf;

    public boolean isInitialized(String realmName) {
		if (!conf.isOAuth2Enabled()) {
			return false;
		}

		if(!realmNameToRealm.containsKey(realmName)) {
			if (kc == null) {
				initializeKeycloakAndMasterRealm();
				synchronized (this) {
					if (kc == null) {
						this.initializeKeycloakAndMasterRealm();

					}
				}
			}

			String upsiRealmName = conf.getUpsiRealm();
			RealmResource realm = kc.realms().realm(realmName);
			if(realm != null) {
				setRealmConfiguration(realmName, realm);

			} else if(!realmNameToRealm.containsKey(conf.getUpsiRealm())) {
				RealmResource upsiRealmResource = kc.realms().realm(upsiRealmName);
				setRealmConfiguration(upsiRealmName, upsiRealmResource);
			}

			return true;

		}else {
			return true;

		}
	}

	private synchronized void initializeKeycloakAndMasterRealm() {
		String keycloakPath = conf.getOAuth2Url();
		String upsMasterRealmName = conf.getUpsMasterRealm();
		String cliClientId = conf.getAdminClient();
		String userName = conf.getAdminUserName();
		String userPassword = conf.getAdminPassword();

		this.kc = KeycloakBuilder.builder() //
				.serverUrl(keycloakPath) //
				.realm(upsMasterRealmName)//
				.username(userName) //
				.password(userPassword) //
				.clientId(cliClientId) //
				.resteasyClient( //
						// Setting TTL to 10 seconds, prevent KC token
						// expiration.
						new ResteasyClientBuilder().connectionPoolSize(25).connectionTTL(10, TimeUnit.SECONDS).build()) //
				.build();

		RealmResource masterRealm = this.kc.realm(upsMasterRealmName);
		setRealmConfiguration(upsMasterRealmName, masterRealm);

		oauth2Enabled = conf.isOAuth2Enabled();
	}

	private void setRealmConfiguration(String realmName, RealmResource realm) {
		RealmRepresentation realmRepresentation = realm.toRepresentation();
		realmRepresentation.setRememberMe(true);
		realmRepresentation.setResetPasswordAllowed(true);
		realmNameToRealm.put(realmName, realm);
	}

	@Override
	public void createClientIfAbsent(PushApplication pushApplication) {
		String applicationName = pushApplication.getName().toLowerCase();
		String realmName = getRealmName(applicationName);
		if (!isInitialized(realmName)) {
			return;
		}

		String clientName = CLIENT_PREFIX + applicationName;
		ClientRepresentation clientRepresentation = isClientExists(pushApplication, realmName);

		if (this.oauth2Enabled && clientRepresentation == null) {
			clientRepresentation = new ClientRepresentation();

			clientRepresentation.setId(clientName);
			clientRepresentation.setClientId(clientName);
			clientRepresentation.setEnabled(true);

			String domain = conf.getRooturlDomain();
			String protocol = conf.getRooturlProtocol();
			clientRepresentation.setRootUrl(conf.getRooturlMatcher().rootUrl(protocol, domain, applicationName));
			// localhost is required for cordova redirect
			clientRepresentation.setRedirectUris(Arrays.asList("/*", "http://localhost"));
			clientRepresentation.setBaseUrl("/");
			clientRepresentation.setAdminUrl("/");

			clientRepresentation.setStandardFlowEnabled(true);
			clientRepresentation.setPublicClient(true);
			clientRepresentation.setWebOrigins(Arrays.asList("*"));

			clientRepresentation.setAttributes(getClientAttributes(pushApplication));
			getRealm(realmName).clients().create(clientRepresentation);
		} else {
			ClientResource clientResource = getRealm(realmName).clients().get(clientRepresentation.getId());
			clientRepresentation.setAttributes(getClientAttributes(pushApplication));
			clientResource.update(clientRepresentation);
			// Evict from cache
			evict(clientRepresentation.getId());
		}
	}

	public void removeClient(PushApplication pushApplication) {
		String realmName = getRealmName(pushApplication.getName());
		if (!isInitialized(realmName)) {
			return;
		}

		ClientRepresentation client = isClientExists(pushApplication, realmName);

		if (client != null) {
			getRealm(realmName).clients().get(client.getClientId()).remove();
		}
	}

	/**
	 * Create verified user by username (If Absent).
	 *
	 * Create user must be done synchronously and prevent clients from
	 * authenticating before KC operation is complete.
	 *  @param userName unique userName
	 * @param password password
	 * @param userTenantInfos tenant infos of the user, to be added as keycloak attribute
	 */
	public void createVerifiedUserIfAbsent(String userName, String password, Collection<UserTenantInfo> userTenantInfos, String realmName) {
		if (!isInitialized(realmName)) {
			return;
		}

		UserRepresentation user = getUser(userName, realmName);

		if (user == null) {
			user = create(userName, password, true, userTenantInfos);

			getRealm(realmName).users().create(user);

			// TODO - Improve implementation, check why we need to update the
			// user right upon creation. without calling updateUserPassword
			// password is invalid.
			if (StringUtils.isNotEmpty(password)) {
				updateUserPassword(userName, password, password, realmName);
			}
		} else {
			logger.debug("KC Username {}, already exist", userName);
		}
	}

	private UserRepresentation create(String userName, String password, boolean enabled, Collection<UserTenantInfo> userTenantInfos) {
		UserRepresentation user = new UserRepresentation();
		user.setUsername(userName);

		user.setRequiredActions(Arrays.asList(UPDATE_PASSWORD_ACTION));
		user.setRealmRoles(Collections.singletonList(KEYCLOAK_ROLE_USER));

		user.setEnabled(enabled);

		if (StringUtils.isNotEmpty(password)) {
			user.setEmailVerified(true);
			user.setEmail(userName);

			user.setCredentials(Arrays.asList(getUserCredentials(password, false)));
		}
		if (userTenantInfos == null) {
			logger.error("create(username={}) no userTenantInfos received", userName);
			throw new IllegalStateException("Missing userTenantInfos for username " + userName);
		} else {
			try {
				setTenantRelationsAsAttribute(userTenantInfos, user);
			} catch (JsonProcessingException e) {
				logger.error("create(username={}) Failed ({}) to write identifier={} : {}", userName,
						e.getClass().getSimpleName(), userTenantInfos, e.getMessage());
				throw new RuntimeException("Failed to write identifier=" + userTenantInfos + " username=" + userName, e);
			}
		}

		return user;
	}

	public boolean exists(String userName, String applicationName) {
		String realmName = getRealmName(applicationName);
		if (!isInitialized(realmName)) {
			return false;
		}

		UserRepresentation user = getUser(userName, realmName);
		if (user == null) {
			logger.debug(String.format("Unable to find user %s, in keyclock", userName));
			return false;
		}

		return true;
	}

	@Async
	public void delete(String userName, String applicationName) {
		String realmName = getRealmName(applicationName);
		if (!isInitialized(realmName)) {
			return;
		}

		if (StringUtils.isEmpty(userName)) {
			logger.warn("Cancel attempt to remove empty or null username");
			return;
		}

		UserRepresentation user = getUser(userName, realmName);
		if (user == null) {
			logger.debug(String.format("Unable to find user %s, in keyclock", userName));
			return;
		}

		getRealm(realmName).users().delete(user.getId());
	}

	@Override
	public List<String> getVariantIdsFromClient(String clientId, String realm) {
		if (!isInitialized(realm)) {
			return null;
		}

		ClientRepresentation client = isClientExists(clientId, realm);

		List<String> variantIds = null;
		if (client != null) {
			Map<String, String> attributes = client.getAttributes();
			if (attributes != null) {
				variantIds = new ArrayList<String>(attributes.size());
				for (Map.Entry<String, String> entry : attributes.entrySet()) {
					if (entry.getKey().endsWith(ATTRIBUTE_VARIANT_SUFFIX)) {
						variantIds.add(entry.getValue());
					}
				}
			}
		}

		return variantIds;
	}

	@Override
	public void resetUserPassword(String aliasId, String newPassword, String applicationName) {
		updateUserPassword(aliasId, null, newPassword, true, getRealmName(applicationName));
	}

	@Override
	public void updateUserPassword(String aliasId, String currentPassword, String newPassword, String applicationName) {
		updateUserPassword(aliasId, currentPassword, newPassword, false, getRealmName(applicationName));
	}

	private void updateUserPassword(String aliasId, String currentPassword, String newPassword, boolean temp, String realmName) {
		UserRepresentation user = getUser(aliasId, realmName);
		if (user == null) {
			logger.debug(String.format("Unable to find user %s, in keyclock", aliasId));
			return;
		}

		boolean isCurrentPasswordValid = isCurrentPasswordValid(user, currentPassword);

		if (isCurrentPasswordValid == true || temp) {
			UsersResource users = getRealm(realmName).users();
			UserResource userResource = users.get(user.getId());

			userResource.resetPassword(getUserCredentials(newPassword, temp));
		}
	}

	private boolean isCurrentPasswordValid(UserRepresentation user, String currentPassword) {
		// TODO: add current password validations
		return true;
	}

	private CredentialRepresentation getUserCredentials(String password, boolean tmp) {
		CredentialRepresentation credential = new CredentialRepresentation();
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(password);
		credential.setTemporary(tmp);

		return credential;
	}

	private UserRepresentation getUser(String username, String realmName) {
		List<UserRepresentation> users = getRealm(realmName).users().search(username);
		return users == null
			? null
			: users.stream()
				.filter(u -> username.equalsIgnoreCase(u.getUsername()))
				.findFirst()
				.orElse(null);
	}

	private ClientRepresentation isClientExists(PushApplication pushApp, String realm) {
		return isClientExists(getClientId(pushApp), realm);
	}

	public static String getClientId(PushApplication pushApp) {
		return CLIENT_PREFIX + pushApp.getName().toLowerCase();
	}

	public static String stripClientPrefix(String clientId) {
		if (StringUtils.isEmpty(clientId))
			return null;
		return clientId.replace(CLIENT_PREFIX, "");
	}

	private ClientRepresentation isClientExists(String clientId, String realm) {
		List<ClientRepresentation> clients = getRealm(realm).clients().findByClientId(clientId);

		if (clients == null | clients.size() == 0) {
			return null;
		}

		// Return first client
		return clients.get(0);
	}

	private Map<String, String> getClientAttributes(PushApplication pushApp) {
		List<Variant> variants = pushApp.getVariants();
		Map<String, String> attributes = new HashMap<>(variants.size());
		for (Variant variant : variants) {
			String varName = variant.getName().toLowerCase();
			attributes.put(varName + ATTRIBUTE_VARIANT_SUFFIX, variant.getVariantID());
			attributes.put(varName + ATTRIBUTE_SECRET_SUFFIX, variant.getSecret());
		}

		return attributes;
	}

	private void evict(String clientId) {
		Cache cache = cacheManager.getCache(IKeycloakService.CACHE_NAME);
		cache.evict(clientId);
	}

	/*
	 * Strip and return subdomain/domain according to matcher and separator.
	 * separator character can be either '-' or '.' or '*'; TODO - Make sure
	 * application name is unique and valid domain.
	 */
	public String strip(String fqdn) {
		String domain = conf.getRooturlDomain();
		DomainMatcher matcher = conf.getRooturlMatcher();

		if (StringUtils.isNotEmpty(fqdn)) {
			return matcher.matches(domain);
		}

		return StringUtils.EMPTY;
	}

	public String seperator() {
		return conf.getRooturlMatcher().seperator();
	}

	public static final int BULK_SIZE = 100;

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private static final ObjectWriter WRITER = MAPPER.writer();

	@Override
	public void updateTenantsExistingUser(String representativeAlias, Collection<UserTenantInfo> tenantRelations, String applicationName) {
		String realmName = getRealmName(applicationName);
		UserRepresentation user = getUser(representativeAlias, realmName);
		UsersResource users = getRealm(realmName).users();
		UserResource userResource = users.get(user.getId());
		try {
			setTenantRelationsAsAttribute(tenantRelations, user);
		} catch (JsonProcessingException e) {
			logger.error("updateTenantsExistingUser(representativeAlias={}) Failed ({}) to write identifiers={} : {}", representativeAlias,
					e.getClass().getSimpleName(), tenantRelations, e.getMessage());
			throw new RuntimeException("Failed to write identifier=" + tenantRelations + " representativeAlias=" + representativeAlias, e);
		}
		userResource.update(user);
	}

	private void setTenantRelationsAsAttribute(Collection<UserTenantInfo> tenantRelations, UserRepresentation user) throws JsonProcessingException {
		user.setAttributes(Collections.singletonMap(USER_TENANT_RELATIONS, toTenantInfosString(tenantRelations)));
	}

	private int updateUsersAttributeChunk(Map<String, ? extends Collection<UserTenantInfo>> aliasToIdentifiers, int updated,
										  UsersResource users, int iterations, int remainder) {
		List<UserRepresentation> remainderUsers = users.list(iterations * BULK_SIZE, remainder);
		updated += updateUsers(aliasToIdentifiers, users, remainderUsers);
		return updated;
	}

	private int updateUsers(Map<String, ? extends Collection<UserTenantInfo>> aliasToIdentifiers, UsersResource users,
							List<UserRepresentation> bulkUsers) {
		int total = 0;
		for (UserRepresentation userRepresentation : bulkUsers) {
			String username = userRepresentation.getUsername();
			Collection<UserTenantInfo> userTenantInfos = aliasToIdentifiers.get(username);
			if (userTenantInfos == null) {
				logger.warn("updateUsers() Found KC user={} without record in cassandra", username);
				continue;
			}
			UserResource userResource = users.get(userRepresentation.getId());
			try {
				setTenantRelationsAsAttribute(userTenantInfos, userRepresentation);
			} catch (JsonProcessingException e) {
				logger.warn("updateUsers() failed ({}) to write user identifiers for user={}: {}",
						e.getClass().getSimpleName(), username, e.getMessage());
				continue;
			}
			try {
				userResource.update(userRepresentation);
			} catch (Exception e) {
				logger.warn("updateUsers() failed ({}) to write user identifiers for user={}, userRepresentation={}: {}",
						e.getClass().getSimpleName(), username, userRepresentation, e.getMessage());
				continue;
			}
			total++;
		}
		return total;
	}

	private static List<String> toTenantInfosString(Collection<UserTenantInfo> userTenantInfos) throws JsonProcessingException {
		List<String> jsons = new ArrayList<>();
		for (UserTenantInfo info : userTenantInfos) {
			String asJson = WRITER.writeValueAsString(info);
			jsons.add(asJson);
		}
		return jsons;
	}

	public String getRealmName(String applicationName) {
		applicationName = applicationName.toLowerCase();
		return kc.realms().realm(applicationName) != null ? applicationName : conf.getUpsiRealm();
	}

	private RealmResource getRealm(String realmName) {
		return realmNameToRealm.get(realmName);
	}
}
