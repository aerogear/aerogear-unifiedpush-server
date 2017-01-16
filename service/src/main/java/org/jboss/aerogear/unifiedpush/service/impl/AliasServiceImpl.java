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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.AliasCrudService;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.KeycloakService;
import org.jboss.aerogear.unifiedpush.service.MergeResponse;
import org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.utils.UUIDs;

@Stateless
public class AliasServiceImpl implements AliasService {
	private final Logger logger = LoggerFactory.getLogger(AliasServiceImpl.class);

	@Inject
	private AliasCrudService aliasCrudService;
	@Inject
	private KeycloakService keycloakService;
	@Inject
	private ClientInstallationService clientInstallationService;

	@Override
	public void updateAliasesAndInstallations(PushApplication pushApplication, List<String> aliases, boolean oauth2) {
		logger.debug("synchronize aliases oauth2 flag is: " + oauth2);

		// Create keycloak client if missing
		if (oauth2) {
			keycloakService.createClientIfAbsent(pushApplication);
		}

		// Remove all aliases from Alias Table
		removeAll(UUID.fromString(pushApplication.getPushApplicationID()));

		// Enable existing aliases / Disable missing aliases (DB Only)
		MergeResponse mergeResponse = clientInstallationService.syncInstallationByAliasList(pushApplication, aliases);

		// Recreate all aliases to Alias Table
		createAliases(pushApplication, aliases);

		// synchronize aliases to keycloak
		if (oauth2) {
			keycloakService.synchronizeUsers(mergeResponse, pushApplication, aliases);
		}
	}

	@Override
	public void updateAliasePassword(String aliasId, String currentPassword, String newPassword) {
		keycloakService.updateUserPassword(aliasId, currentPassword, newPassword);
	}

	@Override
	public void remove(String pushApplicationId, String alias) {
		aliasCrudService.remove(pushApplicationId, alias);
	}

	@Override
	public Alias find(String pushApplicationId, String alias) {
		return aliasCrudService.find(pushApplicationId, alias);
	}

	@Override

	/**
	 * @param alias
	 *            Return first existing and enabled device according to a given
	 *            alias.
	 */
	public Installation exists(String alias) {
		List<Installation> devices = clientInstallationService.findByAlias(alias);

		if (devices == null || devices.size() == 0)
			return null;

		List<Installation> enabledDevices = devices.stream().filter(device -> device.isEnabled())
				.collect(Collectors.toList());

		if (enabledDevices != null && enabledDevices.size() > 0) {
			return enabledDevices.get(0);
		}
		return null;
	}

	@Override
	public Alias create(String pushApplicationId, String alias){
		EmailValidator validator = new EmailValidator();
		return createAlias(UUID.fromString(pushApplicationId), alias, validator);
	}

	private void createAliases(PushApplication pushApp, List<String> aliases) {
		EmailValidator validator = new EmailValidator();
		Set<String> aliasSet = new HashSet<>(aliases);

		for (String name : aliasSet) {
			createAlias(UUID.fromString(pushApp.getPushApplicationID()), name, validator);
		}
	}

	private Alias createAlias(UUID pushApp, String alias, EmailValidator validator) {
		Alias user = new Alias(pushApp, UUIDs.timeBased());
		if (validator.isValid(alias, null)) {
			user.setEmail(alias);
		} else {
			user.setMobile(alias);
		}

		create(user);

		return user;
	}

	@Override
	public void removeAll(UUID pushApplicationId) {
		aliasCrudService.removeAll(pushApplicationId);
	}

	@Override
	public void create(Alias alias) {
		aliasCrudService.create(alias);
	}

	@Override
	@Asynchronous
	public void createAsynchronous(Alias alias) {
		aliasCrudService.create(alias);
	}
}