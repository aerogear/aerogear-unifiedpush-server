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
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import com.datastax.driver.core.utils.UUIDs;

@Transactional
public class PushApplicationServiceTest extends AbstractCassandraServiceTest {

	@Inject
	private DocumentService documentService;

	@Inject
	private AliasService aliasService;

	@Test
	public void addPushApplication() {

		PushApplication pa = new PushApplication();
		pa.setName("EJB Container");
		final String uuid = UUID.randomUUID().toString();
		pa.setPushApplicationID(uuid);

		pushApplicationService.addPushApplication(pa, new LoggedInUser(DEFAULT_USER));

		PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
		assertThat(stored).isNotNull();
		assertThat(stored.getId()).isNotNull();
		assertThat(pa.getName()).isEqualTo(stored.getName());
		assertThat(pa.getPushApplicationID()).isEqualTo(stored.getPushApplicationID());
	}

	@Test
	public void updatePushApplication() {
		PushApplication pa = new PushApplication();
		pa.setName("EJB Container");
		final String uuid = UUID.randomUUID().toString();
		pa.setPushApplicationID(uuid);

		pushApplicationService.addPushApplication(pa, new LoggedInUser(DEFAULT_USER));

		PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
		assertThat(stored).isNotNull();

		stored.setName("FOO");
		pushApplicationService.updatePushApplication(stored);
		stored = pushApplicationService.findByPushApplicationID(uuid);
		assertThat("FOO").isEqualTo(stored.getName());
	}

	@Test
	public void findByPushApplicationID() {
		PushApplication pa = new PushApplication();
		pa.setName("EJB Container");
		final String uuid = UUID.randomUUID().toString();
		pa.setPushApplicationID(uuid);

		pushApplicationService.addPushApplication(pa, new LoggedInUser(DEFAULT_USER));

		PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
		assertThat(stored).isNotNull();
		assertThat(stored.getId()).isNotNull();
		assertThat(pa.getName()).isEqualTo(stored.getName());
		assertThat(pa.getPushApplicationID()).isEqualTo(stored.getPushApplicationID());

		stored = pushApplicationService.findByPushApplicationID("123");
		assertThat(stored).isNull();

	}

	@Test
	public void findAllPushApplicationsForDeveloper() {
		PushApplication pa = new PushApplication();
		pa.setName("EJB Container");
		final String uuid = UUID.randomUUID().toString();
		pa.setPushApplicationID(uuid);
		pa.setDeveloper(DEFAULT_USER);

		pushApplicationService.addPushApplication(pa, new LoggedInUser(DEFAULT_USER));

		assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).isNotEmpty();
		assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).hasSize(2);
	}

	@Test
	public void removePushApplication() {
		PushApplication pa = new PushApplication();
		pa.setName("EJB Container");
		final String uuid = UUID.randomUUID().toString();
		pa.setPushApplicationID(uuid);
		pa.setDeveloper("admin");

		pushApplicationService.addPushApplication(pa, new LoggedInUser(DEFAULT_USER));

		assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).isNotEmpty();
		assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).hasSize(2);

		pushApplicationService.removePushApplication(pa);

		assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).hasSize(1);
		assertThat(pushApplicationService.findByPushApplicationID(uuid)).isNull();
	}

	@Test
	public void findByPushApplicationIDForDeveloper() {
		PushApplication pa = new PushApplication();
		pa.setName("EJB Container");
		final String uuid = UUID.randomUUID().toString();
		pa.setPushApplicationID(uuid);
		pa.setDeveloper("admin");

		pushApplicationService.addPushApplication(pa, new LoggedInUser(DEFAULT_USER));

		PushApplication queried = searchApplicationService.findByPushApplicationIDForDeveloper(uuid);
		assertThat(queried).isNotNull();
		assertThat(uuid).isEqualTo(queried.getPushApplicationID());

		assertThat(searchApplicationService.findByPushApplicationIDForDeveloper("123-3421")).isNull();
	}

	@Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorWhenCreatingAppWithExistingID() {
        // Given
        final String uuid = UUID.randomUUID().toString();

        final PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        pa.setPushApplicationID(uuid);

        final PushApplication pa2 = new PushApplication();
        pa2.setName("EJB Container 2");
        pa2.setPushApplicationID(uuid);

        // When
        pushApplicationService.addPushApplication(pa, new LoggedInUser(DEFAULT_USER));
        assertThat(pushApplicationService.findByPushApplicationID(pa.getPushApplicationID()))
                .isNotNull();

        // Then
        pushApplicationService.addPushApplication(pa2, new LoggedInUser(DEFAULT_USER));
    }

	@Test
	public void removeApplicationAndDocuments() {
		PushApplication pa = new PushApplication();
		pa.setName("EJB Container");
		final String uuid = UUID.randomUUID().toString();
		pa.setPushApplicationID(uuid);
		pa.setDeveloper("admin");

		pushApplicationService.addPushApplication(pa, new LoggedInUser(DEFAULT_USER));

		// Create Alias
		Alias alias = new Alias(UUID.fromString(pa.getPushApplicationID()), UUIDs.timeBased(), "TEST@X.com");
		aliasService.create(alias);

		// Create document for alias
		documentService.save(new DocumentMetadata(pa.getPushApplicationID(), "TASKS", alias), "{SIMPLE}", "1");

		// Query document for alias
		DocumentContent content = documentService.findLatest(getMetadata(pa, alias.getEmail(), "TASKS"), "1");
		assertTrue(content.getContent().equals("{SIMPLE}"));
		List<DocumentContent> documents = documentService.findLatest(pa, "TASKS", "1", Arrays.asList(alias));
		assertTrue(documents.size() == 1);

		// Recreate alias
		aliasService.create(new Alias(UUID.fromString(pa.getPushApplicationID()), UUIDs.timeBased(), "TEST@X.com"));
		assertTrue(aliasService.find(pa.getPushApplicationID(), alias.getEmail()) != null);

		pushApplicationService.removePushApplication(pa);
		documents = documentService.findLatest(pa, "TASKS", "1", Arrays.asList(alias));
		assertTrue(documents.size() == 1);
		assertTrue(aliasService.find(pa.getPushApplicationID(), alias.getEmail()) == null);
	}

	@Override
	protected void specificSetup() {
	}
}

