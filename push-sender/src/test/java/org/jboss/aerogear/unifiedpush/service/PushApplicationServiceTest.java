/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(Arquillian.class)
public class PushApplicationServiceTest extends AbstractBaseServiceTest {

    @Inject
    EntityManager entityManager;

    @Override
    protected void specificSetup() {
        entityManager.getTransaction().begin();
    }

    @After
    public void rollback() {
        entityManager.getTransaction().rollback();
    }


    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(PushApplicationServiceTest.class)
                .withUtils()
                .withMessageModel()
                .withMockito()
                .withMessaging()
                .withDAOs()
                .forServiceTests()
                .withServices()
                .withApi()
                .withUtils()
                .addClass(AbstractBaseServiceTest.class)
                .addClass(EntityManagerProducer.class)
                //I think arquillian is drunk
                .addMavenDependencies("org.assertj:assertj-core")
                .forServiceTests()
                .as(WebArchive.class);
    }


    @Test
    public void addPushApplication() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);

        pushApplicationService.addPushApplication(pa);

        PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
        assertThat(stored).isNotNull();
        assertThat(stored.getId()).isNotNull();
        assertThat(pa.getName()).isEqualTo(stored.getName());
        assertThat(pa.getPushApplicationID()).isEqualTo(stored.getPushApplicationID());
    }

    @Test
    public void shouldThrowErrorWhenCreatingAppWithExistingID() {
        // Given
        final String uuid = UUID.randomUUID().toString();

        final PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        pa.setPushApplicationID(uuid);

        final PushApplication pa2 = new PushApplication();
        pa2.setName("EJB Container 2");
        pa2.setPushApplicationID(uuid);

        pushApplicationService.addPushApplication(pa);

        assertThat(pushApplicationService.findByPushApplicationID(pa.getPushApplicationID()))
                .isNotNull();

        // Then
        try {
            pushApplicationService.addPushApplication(pa2);
            fail("Should catch exception");
        } catch (Exception ex) {
            Assert.assertEquals("App ID already exists: " + uuid.toString(), ex.getMessage());
        }

    }

    @Test
    public void findByPushApplicationIDForDeveloper() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        PushApplication queried = searchApplicationService.findByPushApplicationIDForDeveloper(uuid);
        assertThat(queried).isNotNull();
        assertThat(uuid).isEqualTo(queried.getPushApplicationID());

        assertThat(searchApplicationService.findByPushApplicationIDForDeveloper("123-3421")).isNull();
    }

    @Test
    public void testUpdatePushApplication() throws InterruptedException {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);

        pushApplicationService.addPushApplication(pa);

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

        pushApplicationService.addPushApplication(pa);

        PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
        assertThat(stored).isNotNull();
        assertThat(stored.getId()).isNotNull();
        assertThat(pa.getName()).isEqualTo(stored.getName());
        assertThat(pa.getPushApplicationID()).isEqualTo(stored.getPushApplicationID());

        stored = pushApplicationService.findByPushApplicationID("123");
        assertThat(stored).isNull();

    }

    @Test
    public void removePushApplication() {

        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).isNotEmpty();
        assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).hasSize(1);

        pushApplicationService.removePushApplication(pa);

        assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).isEmpty();
        assertThat(pushApplicationService.findByPushApplicationID(uuid)).isNull();
    }

    @Test
    public void findAllPushApplicationsForDeveloper() {

        assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).isEmpty();

        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).isNotEmpty();
        assertThat(searchApplicationService.findAllPushApplicationsForDeveloper(0, 10).getResultList()).hasSize(1);
    }
}
