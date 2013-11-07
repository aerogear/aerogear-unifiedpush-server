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
package org.jboss.aerogear.unifiedpush.jpa.dao.impl;

import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.UUID;

import static org.junit.Assert.*;

public class PushApplicationDaoTest {

    private EntityManager entityManager;
    private PushApplicationDaoImpl pushApplicationDao;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        pushApplicationDao = new PushApplicationDaoImpl();
        pushApplicationDao.setEntityManager(entityManager);
    }

    @After
    public void tearDown() {
        entityManager.getTransaction().commit();

        entityManager.close();
    }


    @Test
    public void findAllForDeveloper() {
        final PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = UUID.randomUUID().toString();
        pushApplication1.setPushApplicationID(pushApplicationID1);
        pushApplicationDao.create(pushApplication1);

        final PushApplication pushApplication2 = new PushApplication();
        pushApplication2.setName("Push App 2");
        pushApplication2.setDeveloper("Admin");
        final String pushApplicationID2 = UUID.randomUUID().toString();
        pushApplication2.setPushApplicationID(pushApplicationID2);
        pushApplicationDao.create(pushApplication2);

        final PushApplication pushApplication3 = new PushApplication();
        pushApplication3.setName("Push App 3");
        pushApplication3.setDeveloper("Dave The Drummer");
        final String pushApplicationID3 = UUID.randomUUID().toString();
        pushApplication3.setPushApplicationID(pushApplicationID3);
        pushApplicationDao.create(pushApplication3);

        assertEquals(2, pushApplicationDao.findAllForDeveloper("Admin").size());
        assertEquals(1, pushApplicationDao.findAllForDeveloper("Dave The Drummer").size());
        assertEquals("Push App 3", pushApplicationDao.findAllForDeveloper("Dave The Drummer").get(0).getName());
        assertEquals(0, pushApplicationDao.findAllForDeveloper("Admin The Drummer").size());

    }

    @Test
    public void findByPushApplicationIDForDeveloper() {

        final PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = UUID.randomUUID().toString();
        pushApplication1.setPushApplicationID(pushApplicationID1);
        pushApplicationDao.create(pushApplication1);

        assertNotNull(pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID1, "Admin"));
        assertEquals("Push App 1", pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID1, "Admin").getName());
        assertNull(pushApplicationDao.findByPushApplicationIDForDeveloper("1234", "Admin"));
        assertNull(pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID1, "FooBar"));

    }

    @Test
    public void findByPushApplicationID() {

        final PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = UUID.randomUUID().toString();
        pushApplication1.setPushApplicationID(pushApplicationID1);
        pushApplicationDao.create(pushApplication1);

        assertNotNull(pushApplicationDao.findByPushApplicationID(pushApplicationID1));
        assertEquals("Push App 1", pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName());
        assertNull(pushApplicationDao.findByPushApplicationID("13245632"));
    }

    @Test
    public void updateVariant() {
        final PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = UUID.randomUUID().toString();
        pushApplication1.setPushApplicationID(pushApplicationID1);
        pushApplicationDao.create(pushApplication1);

        assertNotNull(pushApplicationDao.findByPushApplicationID(pushApplicationID1));
        assertEquals("Push App 1", pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName());


        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        assertNotNull(pushApplicationDao.findByPushApplicationID(pushApplicationID1));
        assertEquals("Cool Push App 1", pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName());
    }

    @Test
    public void updateAndDeleteVariant() {
        final PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = UUID.randomUUID().toString();
        pushApplication1.setPushApplicationID(pushApplicationID1);
        pushApplicationDao.create(pushApplication1);

        assertNotNull(pushApplicationDao.findByPushApplicationID(pushApplicationID1));
        assertEquals("Push App 1", pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName());


        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        assertNotNull(pushApplicationDao.findByPushApplicationID(pushApplicationID1));
        assertEquals("Cool Push App 1", pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName());

        pushApplicationDao.delete(pushApplication1);
        assertNull(pushApplicationDao.findByPushApplicationID(pushApplicationID1));
    }

}