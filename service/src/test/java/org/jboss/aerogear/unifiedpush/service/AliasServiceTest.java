/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.jboss.aerogear.unifiedpush.service.impl.spring.OAuth2Configuration;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import com.datastax.driver.core.utils.UUIDs;

public class AliasServiceTest extends AbstractCassandraServiceTest {

	@Inject
	private AliasService aliasService;

	@Test
	@Transactional
	public void testMultipleSync() throws IOException {
		PushApplication pushApplication = new PushApplication();
		UUID pushAppId = UUID.fromString(pushApplication.getPushApplicationID());

		List<Alias> aliasesList = new ArrayList<>();
		aliasesList.add(new Alias(pushAppId, UUIDs.timeBased(), "Supprot@AeroBase.org"));
		aliasesList.add(new Alias(pushAppId, UUIDs.timeBased(), "Test@AeroBase.org"));
		aliasesList.add(new Alias(pushAppId, UUIDs.timeBased(), "Help@AeroBase.org"));
		aliasesList.add(new Alias(pushAppId, UUIDs.timeBased(), "gfgfd337765567"));

		// Sync 4 aliases
		List<Alias> aliases = aliasService.addAll(pushApplication, aliasesList, false);

		// Validate 4 aliases
		aliases.forEach(alias -> {
			assertThat(aliasService.find(alias.getPushApplicationId(), alias.getId())).isNotNull();
		});

		// Sync 2 aliases
		aliasService.addAll(pushApplication, aliasesList.subList(0, 1), false);

		// Validate 4 aliases
		aliases.forEach(alias -> {
			assertThat(aliasService.find(alias.getPushApplicationId(), alias.getId())).isNotNull();
		});
	}

	@Test
	@Transactional
	public void testAddAll() throws IOException {
		PushApplication pushApplication = new PushApplication();
		UUID pushAppId = UUID.fromString(pushApplication.getPushApplicationID());

		Alias[] legacyAliases = new Alias[] { new Alias(pushAppId, UUIDs.timeBased(), "Supprot@AeroBase.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Test@AeroBase.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Help@AeroBase.org") };
		List<Alias> aliasList = Arrays.asList(legacyAliases);

		// Sync 3 aliases
		List<Alias> aliases = aliasService.addAll(pushApplication, aliasList, false);

		// Validate 3 aliases
		aliases.forEach(alias -> {
			assertThat(aliasService.find(alias.getPushApplicationId(), alias.getId())).isNotNull();
		});

		// Sync 2 aliases
		aliasService.addAll(pushApplication, Arrays.asList(legacyAliases[0], legacyAliases[1]), false);

		// Validate 3 aliases
		aliases.forEach(alias -> {
			assertThat(aliasService.find(alias.getPushApplicationId(), alias.getId())).isNotNull();
		});
	}

	@Test
	@Transactional
	public void testRemoveAlias() throws IOException {
		PushApplication pushApplication = new PushApplication();
		UUID pushAppId = UUID.fromString(pushApplication.getPushApplicationID());

		Alias[] legacyAliases = new Alias[] { new Alias(pushAppId, UUIDs.timeBased(), "Supprot@AeroBase.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Test@AeroBase.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Help@AeroBase.org") };
		List<Alias> aliasList = Arrays.asList(legacyAliases);

		// Sync 3 aliases
		List<Alias> aliases = aliasService.addAll(pushApplication, aliasList, false);

		// Validate 3 aliases
		aliases.forEach(alias -> {
			assertThat(aliasService.find(alias.getPushApplicationId(), alias.getId())).isNotNull();
		});

		// Delete alias
		aliasService.remove(pushAppId, legacyAliases[0].getEmail());

		// Validate Alias is missing
		assertThat(aliasService.find(legacyAliases[0].getPushApplicationId(), legacyAliases[0].getId())).isNull();

	}

	@Test
	@Transactional
	public void addPushApplication() {
		String domain = "aerobase.com";
		String appName = "xxx";

		System.setProperty(OAuth2Configuration.KEY_OAUTH2_ENFORE_DOMAIN, domain);

		PushApplication pushApplication = new PushApplication();
		pushApplication.setName(appName);
		UUID pushAppId = UUID.randomUUID();
		pushApplication.setPushApplicationID(pushAppId.toString());

		pushApplicationService.addPushApplication(pushApplication, new LoggedInUser(DEFAULT_USER));

		Alias[] aliases = new Alias[] { new Alias(pushAppId, UUIDs.timeBased(), "Supprot@AeroBase.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Test@AeroBase.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Help@AeroBase.org") };
		List<Alias> aliasList = Arrays.asList(aliases);

		// Sync 3 aliases
		aliasService.addAll(pushApplication, aliasList, false);
		boolean result = aliasService.associated(aliases[0].getEmail(), appName + "-" + domain);
		assertThat(result).isTrue();

		result = aliasService.associated(aliases[0].getEmail(), null);
		assertThat(result).isTrue();
	}

	@Override
	protected void specificSetup() {

	}

}