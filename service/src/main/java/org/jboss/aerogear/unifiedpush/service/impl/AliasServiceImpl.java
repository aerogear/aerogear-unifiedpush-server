/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullAlias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.User;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.UserKey;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.PostDelete;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.KeycloakServiceImpl;
import org.jboss.aerogear.unifiedpush.spring.ServiceCacheConfig;
import org.jboss.aerogear.unifiedpush.spring.ServiceCacheConfig.ClusterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.utils.UUIDs;

@Service
public class AliasServiceImpl implements AliasService {
	private final Logger logger = LoggerFactory.getLogger(AliasServiceImpl.class);

	@Inject
	private AliasDao aliasDao;
	@Inject
	private IKeycloakService keycloakService;
	@Inject
	private PushApplicationService pushApplicationService;
	@Inject
	private DocumentService documentService;
	@Inject
	protected ServiceCacheConfig cacheService;

	@Override
	public List<Alias> addAll(PushApplication pushApplication, List<Alias> aliases, boolean oauth2) {
		logger.debug("OAuth2 flag is: " + oauth2);
		List<Alias> aliasList = new ArrayList<>();

		// Create keycloak client if missing.
		if (oauth2)
			keycloakService.createClientIfAbsent(pushApplication);

		aliases.forEach(alias -> {
			create(alias);
			aliasList.add(alias);
		});

		return aliasList;
	}

	@Override
	public void updateAliasePassword(String aliasId, String currentPassword, String newPassword) {
		keycloakService.updateUserPassword(aliasId, currentPassword, newPassword);
	}

	@Override
	public List<UserKey> remove(UUID pushApplicationId, String alias) {
		// Remove any aliases related to this alias name
		return remove(pushApplicationId, alias, false);
	}

	@Override
	public List<UserKey> remove(UUID pushApplicationId, UUID userId) {
		return remove(pushApplicationId, userId, false);
	}

	@Override
	public List<UserKey> remove(UUID pushApplicationId, UUID userId, boolean destructive) {
		Alias alias = aliasDao.findOne(pushApplicationId, userId);
		if (alias == null) {
			return Collections.emptyList();
		}
		return this.remove(pushApplicationId,
				StringUtils.isNotEmpty(alias.getEmail()) ? alias.getEmail() : alias.getOther(), destructive);
	}

	private List<UserKey> remove(UUID pushApplicationId, String alias, boolean destructive) {
		if (destructive) {
			// Remove user from keyCloak
			keycloakService.delete(alias);

			documentService.delete(pushApplicationId, find(pushApplicationId.toString(), alias));
		}

		cacheService.getClusterEventsCache().put(UUID.randomUUID(), ClusterEvent.forAlias(pushApplicationId, alias));

		// Remove any aliases belong to user_id
		return aliasDao.remove(pushApplicationId, alias);
	}

	@Override
	public Alias find(String pushApplicationId, String alias) {
		if (StringUtils.isEmpty(alias))
			return NullAlias.getAlias(pushApplicationId);

		return aliasDao.findByAlias(StringUtils.isEmpty(pushApplicationId) ? null : UUID.fromString(pushApplicationId),
				alias);
	}

	@Override
	public Alias find(UUID pushApplicationId, UUID userId) {
		return aliasDao.findOne(pushApplicationId, userId);
	}

	/**
	 * Test if user exists / registered to KC.
	 *
	 * @param alias alias name
	 */
	@Override
	public boolean registered(String alias) {
		return keycloakService.exists(alias);
	}

	/**
	 * Validate rather an alias is associated to a team/application.
	 *
	 * @param alias alias name
	 * @param fqdn  domain / team name.
	 */
	@Override
	public Associated associated(String alias, String fqdn) {
		PushApplication pushApplication = null;

		// Return application name from fqdn.
		if (StringUtils.isNotEmpty(fqdn)) {
			String applicationName = keycloakService.strip(fqdn);
			pushApplication = pushApplicationService.findByName(applicationName);
		}

		Alias aliasObj = find(pushApplication == null ? null : pushApplication.getPushApplicationID(), alias);

		if (aliasObj != null)
			return new Associated(true, getClientId(aliasObj.getPushApplicationId()), keycloakService.seperator());

		return new Associated(false);
	}

	private Alias exists(UUID pushApplicationUUID, Alias aliasToFind) {
		Alias alias = null;
		if (aliasToFind.getId() != null) {
			alias = aliasDao.findOne(pushApplicationUUID, aliasToFind.getId());

			if (alias != null)
				return alias;
		}

		if (StringUtils.isNotEmpty(aliasToFind.getEmail())) {
			alias = aliasDao.findByAlias(pushApplicationUUID, aliasToFind.getEmail());
			if (alias != null)
				return alias;
		}

		if (StringUtils.isNotEmpty(aliasToFind.getOther())) {
			alias = aliasDao.findByAlias(pushApplicationUUID, aliasToFind.getOther());
			if (alias != null)
				return alias;
		}

		return alias;
	}

	private String getClientId(UUID pushApplicationUUID) {
		PushApplication pushApp = pushApplicationService.findByPushApplicationID(pushApplicationUUID.toString());
		return KeycloakServiceImpl.getClientId(pushApp);
	}

	/*
	 * Remove all aliases by application id and invalidates alias cache. destructive
	 * - when true also remove KC entities and related documents
	 */
	@Override
	@Async
	public void removeAll(PushApplication pushApplication, boolean destructive, PostDelete action) {
		UUID pushApplicationId = UUID.fromString(pushApplication.getPushApplicationID());

		aliasDao.findUserIds(pushApplicationId).map(row -> aliasDao.findOne(pushApplicationId, row.getUUID(0)))
				.filter(alias -> Objects.nonNull(alias)).forEach(alias -> {

					// If not destructive, only aliases are deleted.
					if (destructive) {
						// KC users are registered by email
						if (StringUtils.isNotEmpty(alias.getEmail()))
							keycloakService.delete(alias.getEmail());

						documentService.delete(pushApplicationId, alias);
					}

					aliasDao.remove(pushApplicationId, alias.getId());
				});

		if (destructive) {
			keycloakService.removeClient(pushApplication);
		}

		action.after();
	}

	/**
	 * Create alias while preserving user uuid.
	 */
	@Override
	public void create(Alias alias) {
		// Initialize a new time-based UUID on case one is missing.
		if (alias.getId() == null) {
			// Search if alias is already register for application.
			// If so, use the same userId in-order to keep previous history.
			Alias existingAlias = exists(alias.getPushApplicationId(), alias);

			if (existingAlias != null) {
				// Remove all references to previous alias
				remove(alias.getPushApplicationId(), existingAlias.getId());
				// TODO - if user exists with KC, and primary email changed?
				// Change user alias and enforce registration process

				alias.setId(existingAlias.getId());
			} else {
				alias.setId(UUIDs.timeBased());
			}
		}

		List<User> users = aliasDao.create(alias);
		User representative = users.get(0);
		String representativeAlias = representative.getAlias();

		if (registered(representativeAlias)) {
			Set<UserTenantInfo> tenantRelations = getTenantRelations(representativeAlias);
			keycloakService.updateTenantsExistingUser(representativeAlias, tenantRelations);
		}

	}

	@Override
	@Async
	public void createAsynchronous(Alias alias) {
		create(alias);
	}

	@Override
	public int updateKCUsersGuids() {
		Map<String, Set<UserTenantInfo>> aliasToIdentifiers = aliasDao.findAllUserTenantRelations()
				.collect(Collectors.groupingBy(row -> {
							String alias = row.getAlias();
							return alias.toLowerCase(); // alias is case insensitive
						},
						Collectors.mapping(userKeyToTenantInfo(), Collectors.toSet())));

		return keycloakService.updateUserAttribute(aliasToIdentifiers);
	}

	private Function<UserKey, UserTenantInfo> userKeyToTenantInfo() {
		return key -> {
			UUID userGuid = key.getId();
			UUID pushId = key.getPushApplicationId();
			String clientId = getClientId(pushId);
			return new UserTenantInfo(userGuid, pushId, clientId);
		};
	}

	@Override
	public Set<UserTenantInfo> getTenantRelations(String alias) {
		return aliasDao.findUserTenantRelations(alias).map(userKeyToTenantInfo()).collect(Collectors.toSet());
	}

	@Override
	public int addClientScope(String clientScope) {
		return keycloakService.addClientScope(clientScope);
	}

	public class Associated {
		private boolean associated;
		private String client;
		private String seperator;
		private String subdomain;

		public Associated(boolean associated, String client) {
			super();
			this.associated = associated;
			this.client = client;
		}

		public Associated(boolean associated, String client, String seperator) {
			super();
			this.associated = associated;
			this.client = client;
			this.seperator = seperator;
		}

		public Associated(boolean associated) {
			super();
			this.associated = associated;
		}

		public boolean isAssociated() {
			return associated;
		}

		public String getClient() {
			return client;
		}

		public String getSeperator() {
			return seperator;
		}

		public String getSubdomain() {
			return subdomain;
		}

		public void setSubdomain(String subdomain) {
			this.subdomain = subdomain;
		}
	}
}