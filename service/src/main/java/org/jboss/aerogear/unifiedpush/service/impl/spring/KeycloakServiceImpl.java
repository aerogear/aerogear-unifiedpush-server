package org.jboss.aerogear.unifiedpush.service.impl.spring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    private volatile Boolean oauth2Enabled;
	private Keycloak kc;
	private RealmResource realm;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private IOAuth2Configuration conf;

    public boolean isInitialized() {
		if (!conf.isOAuth2Enabled()) {
			return false;
		}

		if (oauth2Enabled == null) {
			synchronized (this) {
				if (oauth2Enabled == null) {

					if (conf.isOAuth2Enabled()) {
						this.initialize();
					}

					oauth2Enabled = conf.isOAuth2Enabled();
				}
			}
		}

		return oauth2Enabled.booleanValue();
	}

	private void initialize() {
		String keycloakPath = conf.getOAuth2Url();

		String upsiRealmId = conf.getUpsiRealm();
		String cliClientId = conf.getAdminClient();
		String userName = conf.getAdminUserName();
		String userPassword = conf.getAdminPassword();

		this.kc = KeycloakBuilder.builder() //
				.serverUrl(keycloakPath) //
				.realm(upsiRealmId)//
				.username(userName) //
				.password(userPassword) //
				.clientId(cliClientId) //
				.resteasyClient( //
						// Setting TTL to 10 seconds, prevent KC token
						// expiration.
						new ResteasyClientBuilder().connectionPoolSize(25).connectionTTL(10, TimeUnit.SECONDS).build()) //
				.build();

		this.realm = this.kc.realm(upsiRealmId);

		setRealmConfiguration();
	}

	private void setRealmConfiguration() {
		RealmRepresentation realmRepresentation = this.realm.toRepresentation();
		realmRepresentation.setRememberMe(true);
		realmRepresentation.setResetPasswordAllowed(true);
	}

	@Override
	public void createClientIfAbsent(PushApplication pushApplication) {
		if (!isInitialized()) {
			return;
		}

		String applicationName = pushApplication.getName().toLowerCase();
		String clientName = CLIENT_PREFIX + applicationName;
		ClientRepresentation clientRepresentation = isClientExists(pushApplication);

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
			this.realm.clients().create(clientRepresentation);
		} else {
			ClientResource clientResource = this.realm.clients().get(clientRepresentation.getId());
			clientRepresentation.setAttributes(getClientAttributes(pushApplication));
			clientResource.update(clientRepresentation);
			// Evict from cache
			evict(clientRepresentation.getId());
		}
	}

	public void removeClient(PushApplication pushApplicaiton) {

		if (!isInitialized()) {
			return;
		}

		ClientRepresentation client = isClientExists(pushApplicaiton);

		if (client != null) {
			this.realm.clients().get(client.getClientId()).remove();
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
	public void createVerifiedUserIfAbsent(String userName, String password, Collection<UserTenantInfo> userTenantInfos) {
		if (!isInitialized()) {
			return;
		}

		UserRepresentation user = getUser(userName);

		if (user == null) {
			user = create(userName, password, true, userTenantInfos);

			this.realm.users().create(user);

			// TODO - Improve implementation, check why we need to update the
			// user right upon creation. without calling updateUserPassword
			// password is invalid.
			if (StringUtils.isNotEmpty(password)) {
				updateUserPassword(userName, password, password);
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

	public boolean exists(String userName) {
		if (!isInitialized()) {
			return false;
		}

		UserRepresentation user = getUser(userName);
		if (user == null) {
			logger.debug(String.format("Unable to find user %s, in keyclock", userName));
			return false;
		}

		return true;
	}

	@Async
	public void delete(String userName) {
		if (!isInitialized()) {
			return;
		}

		if (StringUtils.isEmpty(userName)) {
			logger.warn("Cancel attempt to remove empty or null username");
			return;
		}

		UserRepresentation user = getUser(userName);
		if (user == null) {
			logger.debug(String.format("Unable to find user %s, in keyclock", userName));
			return;
		}

		this.realm.users().delete(user.getId());
	}

	@Override
	public List<String> getVariantIdsFromClient(String clientId) {
		if (!isInitialized()) {
			return null;
		}

		ClientRepresentation client = isClientExists(clientId);

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
	public void resetUserPassword(String aliasId, String newPassword) {
		updateUserPassword(aliasId, null, newPassword, true);
	}

	@Override
	public void updateUserPassword(String aliasId, String currentPassword, String newPassword) {
		updateUserPassword(aliasId, currentPassword, newPassword, false);
	}

	private void updateUserPassword(String aliasId, String currentPassword, String newPassword, boolean temp) {
		UserRepresentation user = getUser(aliasId);
		if (user == null) {
			logger.debug(String.format("Unable to find user %s, in keyclock", aliasId));
			return;
		}

		boolean isCurrentPasswordValid = isCurrentPasswordValid(user, currentPassword);

		if (isCurrentPasswordValid == true || temp) {
			UsersResource users = this.realm.users();
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

	private UserRepresentation getUser(String username) {
		List<UserRepresentation> users = this.realm.users().search(username);
		return users == null
				? null
				: users.stream()
					.filter(u -> username.equalsIgnoreCase(u.getUsername()))
					.findFirst()
					.orElseGet(null);
	}

	private ClientRepresentation isClientExists(PushApplication pushApp) {
		return isClientExists(getClientId(pushApp));
	}

	public static String getClientId(PushApplication pushApp) {
		return CLIENT_PREFIX + pushApp.getName().toLowerCase();
	}

	public static String stripClientPrefix(String clientId) {
		if (StringUtils.isEmpty(clientId))
			return null;
		return clientId.replace(CLIENT_PREFIX, "");
	}

	private ClientRepresentation isClientExists(String clientId) {
		List<ClientRepresentation> clients = this.realm.clients().findByClientId(clientId);

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
	public int addClientScope(String clientScope) {
		if (!isInitialized()) {
			throw new RuntimeException("addClientScope(clientScope=" + clientScope + ") keycloak not initialized");
		}

		int updated = 0;
		List<ClientScopeRepresentation> defaultClientScopes = realm.getDefaultDefaultClientScopes();
		ClientScopeRepresentation clientScopeRepresentation = defaultClientScopes.stream().filter(s -> s.getName().equals(clientScope)).findAny().orElseThrow(() ->
				new RuntimeException("client scope=" + clientScope + " doesn't exist"));
		ClientsResource clients = realm.clients();
		Collection<ClientRepresentation> clientsAll = clients.findAll(true);
		Set<ClientRepresentation> clientIds = getRelevantClientIds(clientsAll);
		for (ClientRepresentation clientRepresentation : clientIds) {
			List<String> clientScopes = clientRepresentation.getDefaultClientScopes();
			if (clientScopes.contains(clientScope)) {
				continue;
			} else {
				String id = clientRepresentation.getId();
				ClientResource clientResource = clients.get(id);
				try {
					clientResource.addDefaultClientScope(clientScopeRepresentation.getId());
				} catch (Exception e) {
					logger.error("addClientScope(clientScope={}) Failed ({}) to add client scope to client={} : {}"
					, clientScope, e.getClass().getSimpleName(), clientRepresentation, e.getMessage());
					continue;
				}
				updated++;
			}
		}

		return updated;
	}

	private Set<ClientRepresentation> getRelevantClientIds(Collection<ClientRepresentation> clientsAll) {
		return clientsAll.stream().filter(c -> c.getClientId().startsWith(CLIENT_PREFIX)).collect(Collectors.toSet());
	}

	@Override
	public int updateUserAttribute(Map<String, ? extends Collection<UserTenantInfo>> aliasToIdentifiers) {
		if (!isInitialized()) {
			throw new RuntimeException("keycloak not initialized");
		}

		int updated = 0;
		UsersResource users = this.realm.users();
		Integer count = users.count();
		int iterations = count / BULK_SIZE;
		for (int i = 0; i < iterations; i++) {
			updated = updateUsersAttributeChunk(aliasToIdentifiers, updated, users, i, BULK_SIZE);
		}
		int remainder = count % BULK_SIZE;
		if (remainder == 0) {
			return updated;
		}

		updated = updateUsersAttributeChunk(aliasToIdentifiers, updated, users, iterations, remainder);

		return updated;
	}

	@Override
	public void updateTenantsExistingUser(String representativeAlias, Collection<UserTenantInfo> tenantRelations) {
		UserRepresentation user = getUser(representativeAlias);
		UsersResource users = this.realm.users();
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
}
