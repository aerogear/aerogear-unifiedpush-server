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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.KeycloakService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;

@Stateless
public class AliasServiceImpl implements AliasService {
	private final Logger logger = LoggerFactory.getLogger(AliasServiceImpl.class);
	private final static EmailValidator EMAIL_VALIDATOR = new EmailValidator();

	@Inject
	private AliasCrudService aliasCrudService;
	@Inject
	private KeycloakService keycloakService;
	@Inject
	private ClientInstallationService clientInstallationService;

	public List<Alias> addAll(PushApplication pushApplication, List<Alias> aliases, boolean oauth2) {
		logger.debug("OAuth2 flag is: " + oauth2);
		List<Alias> aliasList = new ArrayList<>();

		// Create keycloak client if missing.
		if (oauth2)
			keycloakService.createClientIfAbsent(pushApplication);

		aliases.forEach(alias -> {
			create(alias, oauth2);
			aliasList.add(alias);
		});

		return aliasList;
	}

	@Override
	@Deprecated
	public List<Alias> syncAliases(PushApplication pushApplication, List<String> aliases, boolean oauth2) {
		logger.debug("OAuth2 flag is: " + oauth2);

		// Create keycloak client if missing.
		if (oauth2)
			keycloakService.createClientIfAbsent(pushApplication);

		// Recreate all aliases to Alias Table
		List<Alias> aliasList = createAliases(pushApplication, aliases, oauth2);

		return aliasList;
	}

	@Override
	public void updateAliasePassword(String aliasId, String currentPassword, String newPassword) {
		keycloakService.updateUserPassword(aliasId, currentPassword, newPassword);
	}

	@Override
	public void remove(UUID pushApplicationId, String alias) {
		// Remove any aliases related to this alias name
		remove(pushApplicationId, alias, false);
	}

	@Override
	public void remove(UUID pushApplicationId, UUID userId) {
		remove(pushApplicationId, userId, false);
	}

	@Override
	public void remove(UUID pushApplicationId, UUID userId, boolean destructive) {
		Alias alias = aliasCrudService.find(pushApplicationId, userId);
		this.remove(pushApplicationId, StringUtils.isNotEmpty(alias.getEmail()) ? alias.getEmail() : alias.getOther(),
				destructive);
	}

	private void remove(UUID pushApplicationId, String alias, boolean destructive) {
		// Remove any aliases belong to user_id
		aliasCrudService.remove(pushApplicationId, alias);

		if (destructive) {
			// Remove user from keyCloak
			keycloakService.delete(alias);

			// TODO - Remove all documents for a given alias
		}
	}

	@Override
	public Alias find(String pushApplicationId, String alias) {
		return aliasCrudService.find(StringUtils.isEmpty(pushApplicationId) ? null : UUID.fromString(pushApplicationId),
				alias);
	}

	@Override
	public Alias find(UUID pushApplicationId, UUID userId) {
		return aliasCrudService.find(pushApplicationId, userId);
	}

	@Override
	public Installation exists(String alias) {
		return exists(alias, VariantType.SIMPLE_PUSH);
	}

	/**
	 * Return first existing and enabled device according to a given alias and
	 * {@link VariantType}.
	 *
	 * @param alias
	 *            alias name
	 * @param type
	 *            variant type to filter
	 * @return {@link Installation}
	 */
	public Installation exists(String alias, VariantType type) {
		List<Installation> devices = clientInstallationService.findByAlias(alias);

		if (devices == null || devices.size() == 0)
			return null;

		List<Installation> enabledDevices = devices.stream()
				.filter(device -> device.isEnabled() & device.getVariant().getType().equals(type))
				.collect(Collectors.toList());

		if (enabledDevices != null && enabledDevices.size() > 0) {
			return enabledDevices.get(0);
		}

		return null;
	}

	@Override
	public Alias create(String pushApplicationId, String alias) {
		return createAlias(UUID.fromString(pushApplicationId), alias, false);
	}

	/*
	 * Deprecated - Use Alias object from model-api Used only from Deprecated
	 * syncAliases
	 */
	@Deprecated
	private List<Alias> createAliases(PushApplication pushApp, List<String> aliases, boolean oauth2) {
		List<Alias> aliasList = new ArrayList<>();

		for (String name : aliases) {
			aliasList.add(createAlias(UUID.fromString(pushApp.getPushApplicationID()), name, oauth2));
		}

		return aliasList;
	}

	@Deprecated
	private Alias createAlias(UUID pushApp, String alias, boolean oauth2) {
		Alias user = new Alias(pushApp, null);
		if (EMAIL_VALIDATOR.isValid(alias, null)) {
			user.setEmail(alias);
		} else {
			user.setOther(alias);
		}

		create(user, oauth2);

		return user;
	}

	private Alias exists(UUID pushApplicationUUID, Alias aliasToFind) {
		Alias alias = null;
		if (aliasToFind.getId() != null) {
			alias = aliasCrudService.find(pushApplicationUUID, aliasToFind.getId());

			if (alias != null)
				return alias;
		}

		if (StringUtils.isNotEmpty(aliasToFind.getEmail())) {
			alias = aliasCrudService.find(pushApplicationUUID, aliasToFind.getEmail());
			if (alias != null)
				return alias;
		}

		if (StringUtils.isNotEmpty(aliasToFind.getOther())) {
			alias = aliasCrudService.find(pushApplicationUUID, aliasToFind.getOther());
			if (alias != null)
				return alias;
		}

		return alias;
	}

	/*
	 * Remove all aliases by application id and invalidates alias cache.
	 */
	@Override
	public void removeAll(UUID pushApplicationId) {
		aliasCrudService.removeAll(pushApplicationId);

		// TODO - Remove KC client and all related users.
	}

	/**
	 * If
	 */
	@Override
	public void create(Alias alias, boolean oauth2) {
		// Initialize a new time-based UUID on case one is missing.
		if (alias.getId() == null) {
			// Search if alias is already register for application.
			// If so, use the same userId in-order to keep previous history.
			Alias existingAlias = exists(alias.getPushApplicationId(), alias);

			if (existingAlias != null) {
				// Remove all references to previous alias
				remove(alias.getPushApplicationId(), existingAlias.getId());
				// TODO - Since KC users are not removed without destructive
				// flag, We need to update existing KC users and set a new
				// email.

				alias.setId(existingAlias.getId());
			} else {
				alias.setId(UUIDs.timeBased());
			}
		}

		aliasCrudService.create(alias);

		// synchronize aliases to keycloak
		if (oauth2) {
			keycloakService.createUserIfAbsent(alias.getEmail());
		}
	}

	@Override
	@Asynchronous
	public void createAsynchronous(Alias alias, boolean oauth2) {
		create(alias, oauth2);
	}
}