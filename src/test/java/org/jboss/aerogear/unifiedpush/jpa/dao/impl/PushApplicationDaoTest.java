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

import org.jboss.aerogear.unifiedpush.jpa.AbstractGenericDao;
import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.*;

public class PushApplicationDaoTest {

    private EntityManager entityManager;
    private PushApplicationDaoImpl pushApplicationDao;
    private VariantDaoImpl variantDao;
    private InstallationDaoImpl installationDao;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        pushApplicationDao = new PushApplicationDaoImpl();
        pushApplicationDao.setEntityManager(entityManager);
        variantDao = new VariantDaoImpl();
        variantDao.setEntityManager(entityManager);
        installationDao = new InstallationDaoImpl();
        installationDao.setEntityManager(entityManager);
    }

    @After
    public void tearDown() {
        entityManager.getTransaction().commit();

        entityManager.close();
    }


    @Test
    public void findAll() {
        final PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        pushApplicationDao.create(pushApplication1);

        final PushApplication pushApplication2 = new PushApplication();
        pushApplication2.setName("Push App 2");
        pushApplication2.setDeveloper("Admin");
        pushApplicationDao.create(pushApplication2);

        final PushApplication pushApplication3 = new PushApplication();
        pushApplication3.setName("Push App 3");
        pushApplication3.setDeveloper("Dave The Drummer");
        pushApplicationDao.create(pushApplication3);

        assertEquals(3, pushApplicationDao.findAll().size());
    }

    @Test
    public void findAllForDeveloper() {
        final PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        pushApplicationDao.create(pushApplication1);

        final PushApplication pushApplication2 = new PushApplication();
        pushApplication2.setName("Push App 2");
        pushApplication2.setDeveloper("Admin");
        pushApplicationDao.create(pushApplication2);

        final PushApplication pushApplication3 = new PushApplication();
        pushApplication3.setName("Push App 3");
        pushApplication3.setDeveloper("Dave The Drummer");
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
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
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
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
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
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
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
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
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

    @Test
    public void pushApplicationIDUnmodifiedAfterUpdate() {
        final PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
        pushApplicationDao.create(pushApplication1);

        assertNotNull(pushApplicationDao.findByPushApplicationID(pushApplicationID1));
        assertEquals("Push App 1", pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName());


        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        assertNotNull(pushApplicationDao.findByPushApplicationID(pushApplicationID1));
        assertEquals(pushApplicationID1, pushApplicationDao.findByPushApplicationID(pushApplicationID1).getPushApplicationID());
    }

    @Test
    public void primaryKeyUnmodifiedAfterUpdate() {
        PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        final String id = pushApplication1.getId();
        pushApplicationDao.create(pushApplication1);

        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();


        PushApplication pa = pushApplicationDao.find(PushApplication.class, id);

        assertEquals(id, pa.getId());

        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        entityManager.flush();
        entityManager.clear();

        pa = pushApplicationDao.find(PushApplication.class, id);

        assertEquals("Cool Push App 1", pa.getName());
    }

    @Test
    public void deletePushApplicationIncludingVariantAndInstallations() {
        PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        final String id = pushApplication1.getId();
        pushApplicationDao.create(pushApplication1);

        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

        PushApplication pa = pushApplicationDao.find(PushApplication.class, id);
        assertEquals(id, pa.getId());

        AndroidVariant av = new AndroidVariant();
        av.setName("Android Variant");
        av.setGoogleKey("KEY...");
        variantDao.create(av);

        InstallationImpl androidInstallation1 = new InstallationImpl();
        androidInstallation1.setDeviceToken("12345432122323");
        installationDao.create(androidInstallation1);

        av.getInstallations().add(androidInstallation1);
        variantDao.update(av);

        pa.getAndroidVariants().add(av);
        pushApplicationDao.update(pa);

        assertNotNull(((AbstractGenericDao) pushApplicationDao).find(InstallationImpl.class, androidInstallation1.getId()));

        pushApplicationDao.delete(pa);
        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

        // Installation should be gone:
        assertNull(((AbstractGenericDao) pushApplicationDao).find(InstallationImpl.class, androidInstallation1.getId()));

        // Variant should be gone:
        assertNull(((AbstractGenericDao) pushApplicationDao).find(AndroidVariant.class, av.getId()));

        // PushApp should be gone:
        assertNull(pushApplicationDao.find(PushApplication.class, id));
    }
}