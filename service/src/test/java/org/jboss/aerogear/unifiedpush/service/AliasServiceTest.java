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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.UserKey;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.jboss.aerogear.unifiedpush.service.impl.AliasServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.AliasServiceImpl.Associated;
import org.jboss.aerogear.unifiedpush.service.impl.spring.OAuth2Configuration;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import com.datastax.driver.core.utils.UUIDs;

public class AliasServiceTest extends AbstractCassandraServiceTest {

	@Inject
	private AliasService aliasService;
	@Inject
	private DocumentService documentService;

	@Test
	@Transactional
	public void testMultipleSync() throws IOException {
		PushApplication pushApplication = new PushApplication();
		UUID pushAppId = UUID.fromString(pushApplication.getPushApplicationID());

		List<Alias> aliasesList = new ArrayList<>();
		aliasesList.add(new Alias(pushAppId, UUIDs.timeBased(), "Supprot@AeroGear.org"));
		aliasesList.add(new Alias(pushAppId, UUIDs.timeBased(), "Test@AeroGear.org"));
		aliasesList.add(new Alias(pushAppId, UUIDs.timeBased(), "Help@AeroGear.org"));
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

		Alias[] legacyAliases = new Alias[] { new Alias(pushAppId, UUIDs.timeBased(), "Supprot@AeroGear.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Test@AeroGear.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Help@AeroGear.org") };
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

		UUID firstGuid = UUIDs.timeBased();
		Alias[] legacyAliases = new Alias[] { new Alias(pushAppId, firstGuid, "Supprot@AeroGear.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Test@AeroGear.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Help@AeroGear.org") };
		List<Alias> aliasList = Arrays.asList(legacyAliases);

		// Sync 3 aliases
		List<Alias> aliases = aliasService.addAll(pushApplication, aliasList, false);

		// Validate 3 aliases
		aliases.forEach(alias -> {
			assertThat(aliasService.find(alias.getPushApplicationId(), alias.getId())).isNotNull();
		});

		documentService.save(new DocumentMetadata(pushAppId, getClass().getSimpleName(), firstGuid),
				"doc1", "test_id");

		// Delete alias
		aliasService.remove(pushAppId, firstGuid, true);

		// Validate Alias is missing
		assertThat(aliasService.find(pushAppId, firstGuid)).isNull();

	}

	@Test
	@Transactional
	public void addPushApplication() {
		String domain = "aerogear.com";
		String appName = "xxx";
		String realmName = "testRealm";
		String hostName = realmName + OAuth2Configuration.DEFAULT_SUBDOMAIN_SEPERATOR + domain;

		System.setProperty(OAuth2Configuration.KEY_OAUTH2_ENFORE_DOMAIN, domain);

		PushApplication pushApplication = new PushApplication();
		pushApplication.setName(appName);
		UUID pushAppId = UUID.randomUUID();
		pushApplication.setPushApplicationID(pushAppId.toString());

		pushApplicationService.addPushApplication(pushApplication, new LoggedInUser(DEFAULT_USER));

		Alias[] aliases = new Alias[] { new Alias(pushAppId, UUIDs.timeBased(), "Supprot@AeroGear.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Test@AeroGear.org"),
				new Alias(pushAppId, UUIDs.timeBased(), "Help@AeroGear.org") };
		List<Alias> aliasList = Arrays.asList(aliases);

		// Sync 3 aliases
		aliasService.addAll(pushApplication, aliasList, false);
		Associated associated = aliasService.associated(aliases[0].getEmail(), appName + "-" + domain, hostName);
	
		assertThat(associated != null && associated.isAssociated()).isTrue();

		associated = aliasService.associated(aliases[0].getEmail(), null, hostName);
		assertThat(associated != null && associated.isAssociated()).isTrue();
	}

	@Override
	protected void specificSetup() {

	}

	@Test
	public void testReducingUserKeysByTheirUUIDTimeStamp() throws InterruptedException {
		Collection<UserKey> users = new ArrayList<>();
		UUID app1 = UUIDs.random();
		UserKey u1 = createUser("alias1", app1);
		UserKey u2 = createUser("alias1", app1);

		users.add(u1);
		users.add(u2);

		UUID app2 = UUIDs.random();
		UserKey u3 = createUser("alias2", app2);
		UserKey u4 = createUser("alias3", app2);
		users.add(u3);
		users.add(u4);

		Map<String, UserKey> expectedResult = new HashMap<>();
		expectedResult.put("alias1", u2); // latest user UUID timestamp
		expectedResult.put("alias2", u3);
		expectedResult.put("alias3", u4);

		Collection<UserKey> result = AliasServiceImpl.reduceUserKeysByUserUUIDTimeStamp(users.stream());
		assertEquals("Mismatched amount of reduced users", 3, result.size());
		for (UserKey resultUserKey : result) {
			String alias = resultUserKey.getAlias();
			UserKey expected = expectedResult.get(alias);
			assertSame(expected, resultUserKey);
		}
	}

	private UserKey createUser(String alias, UUID application) throws InterruptedException {
		UserKey user = new UserKey();
		user.setId(UUIDs.timeBased());
		user.setAlias(alias);
		user.setPushApplicationId(application);
		return user;
	}
}