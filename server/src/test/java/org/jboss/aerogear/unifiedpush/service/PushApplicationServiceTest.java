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

import org.apache.openejb.jee.Beans;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.testing.Module;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushApplicationDao;
import org.jboss.aerogear.unifiedpush.service.impl.PushApplicationServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@RunWith(ApplicationComposer.class)
public class PushApplicationServiceTest {

    @Inject
    private PushApplicationService pushApplicationService;

    @Module
    public Beans getBeans() {
        final Beans beans = new Beans();
        beans.addManagedClass(PushApplicationServiceImpl.class);
        beans.addManagedClass(JPAPushApplicationDao.class);

        return beans;
    }

    @Module
    public Class<?>[] app() throws Exception {
        return new Class<?>[] { EntityManagerProducer.class};
    }

    @Test
    public void addPushApplication() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);

        pushApplicationService.addPushApplication(pa);

        PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
        assertNotNull(stored);
        assertNotNull(stored.getId());
        assertEquals(pa.getName(), stored.getName());
        assertEquals(pa.getPushApplicationID(), stored.getPushApplicationID());
    }

    @Test
    public void updatePushApplication() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);

        pushApplicationService.addPushApplication(pa);

        PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
        assertNotNull(stored);

        stored.setName("FOO");
        pushApplicationService.updatePushApplication(stored);
        stored = pushApplicationService.findByPushApplicationID(uuid);
        assertEquals("FOO", stored.getName());
    }

    @Test
    public void findByPushApplicationID() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);

        pushApplicationService.addPushApplication(pa);

        PushApplication stored = pushApplicationService.findByPushApplicationID(uuid);
        assertNotNull(stored);
        assertNotNull(stored.getId());
        assertEquals(pa.getName(), stored.getName());
        assertEquals(pa.getPushApplicationID(), stored.getPushApplicationID());

        stored = pushApplicationService.findByPushApplicationID("123");
        assertNull(stored);

    }

    @Test
    public void findAllPushApplicationsForDeveloper() {

        assertTrue(pushApplicationService.findAllPushApplicationsForDeveloper("admin").isEmpty());

        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        assertFalse(pushApplicationService.findAllPushApplicationsForDeveloper("admin").isEmpty());
        assertEquals(1, pushApplicationService.findAllPushApplicationsForDeveloper("admin").size());
    }

    @Test
    public void removePushApplication() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        assertFalse(pushApplicationService.findAllPushApplicationsForDeveloper("admin").isEmpty());
        assertEquals(1, pushApplicationService.findAllPushApplicationsForDeveloper("admin").size());

        pushApplicationService.removePushApplication(pa);

        assertTrue(pushApplicationService.findAllPushApplicationsForDeveloper("admin").isEmpty());
        assertNull(pushApplicationService.findByPushApplicationID(uuid));
    }

    @Test
    public void findByPushApplicationIDForDeveloper() {
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);
        pa.setDeveloper("admin");

        pushApplicationService.addPushApplication(pa);

        PushApplication queried =  pushApplicationService.findByPushApplicationIDForDeveloper(uuid, "admin");
        assertNotNull(queried);
        assertEquals(uuid, queried.getPushApplicationID());

        assertNull(pushApplicationService.findByPushApplicationIDForDeveloper(uuid, "admin2"));
        assertNull(pushApplicationService.findByPushApplicationIDForDeveloper("123-3421", "admin"));
    }

    // test-ware: EM producer:

    @SessionScoped
    @Stateful
    public static class EntityManagerProducer implements Serializable {

        {
            EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
            entityManager = emf.createEntityManager();
        }

        private static EntityManager entityManager;

        @Produces
        public EntityManager produceEm() {
            entityManager.getTransaction().begin();
            return entityManager;
        }

        @PreDestroy
        public void closeEntityManager() {
            if (entityManager.isOpen()) {
                entityManager.getTransaction().commit();
                entityManager.close();
            }
        }
    }
}
