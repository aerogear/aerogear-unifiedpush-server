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
package org.jboss.aerogear.unifiedpush.jpa;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushApplicationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.assertj.core.api.Assertions.assertThat;

public class PushApplicationDaoTest {

    private EntityManager entityManager;
    private JPAPushApplicationDao pushApplicationDao;
    private JPAVariantDao variantDao;
    private JPAInstallationDao installationDao;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        pushApplicationDao = new JPAPushApplicationDao();
        pushApplicationDao.setEntityManager(entityManager);
        variantDao = new JPAVariantDao();
        variantDao.setEntityManager(entityManager);
        installationDao = new JPAInstallationDao();
        installationDao.setEntityManager(entityManager);
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
        pushApplicationDao.create(pushApplication1);

        final PushApplication pushApplication2 = new PushApplication();
        pushApplication2.setName("Push App 2");
        pushApplication2.setDeveloper("Admin");
        pushApplicationDao.create(pushApplication2);

        final PushApplication pushApplication3 = new PushApplication();
        pushApplication3.setName("Push App 3");
        pushApplication3.setDeveloper("Dave The Drummer");
        pushApplicationDao.create(pushApplication3);

        assertThat(pushApplicationDao.findAllForDeveloper("Admin")).hasSize(2);
        assertThat(pushApplicationDao.findAllForDeveloper("Dave The Drummer")).hasSize(1);
        assertThat(pushApplicationDao.findAllForDeveloper("Dave The Drummer")).extracting("name").containsOnly("Push App 3");
        assertThat(pushApplicationDao.findAllForDeveloper("Admin The Drummer")).isEmpty();

    }

    @Test
    public void findByPushApplicationIDForDeveloper() {

        final PushApplication pushApplication1 = new PushApplication ();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
        pushApplicationDao.create(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID1, "Admin")).isNotNull();

        assertThat(pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID1, "Admin").getName()).isEqualTo(pushApplication1.getName());

        assertThat(pushApplicationDao.findByPushApplicationIDForDeveloper("1234", "Admin")).isNull();
        assertThat(pushApplicationDao.findByPushApplicationIDForDeveloper(pushApplicationID1, "FooBar")).isNull();

    }

    @Test
    public void findByPushApplicationID() {

        final PushApplication pushApplication1 = new PushApplication ();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
        pushApplicationDao.create(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Push App 1");
        assertThat(pushApplicationDao.findByPushApplicationID("13245632")).isNull();
    }

    @Test
    public void updatePushApplication() {
        final PushApplication pushApplication1 = new PushApplication ();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
        pushApplicationDao.create(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Push App 1");

        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Cool Push App 1");
    }

    @Test
    public void updateAndDeletePushApplication() {
        final PushApplication pushApplication1 = new PushApplication ();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
        pushApplicationDao.create(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Push App 1");


        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Cool Push App 1");

        pushApplicationDao.delete(pushApplication1);
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNull();
    }

    @Test
    public void pushApplicationIDUnmodifiedAfterUpdate() {
        final PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        pushApplication1.setDeveloper("Admin");
        final String pushApplicationID1 = pushApplication1.getPushApplicationID();
        pushApplicationDao.create(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getName()).isEqualTo("Push App 1");


        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1)).isNotNull();
        assertThat(pushApplicationDao.findByPushApplicationID(pushApplicationID1).getPushApplicationID()).isEqualTo(pushApplicationID1);
    }

    @Test
    public void primaryKeyUnmodifiedAfterUpdate() {
        PushApplication pushApplication1 = new PushApplication ();
        pushApplication1.setName("Push App 1");
        final String id = pushApplication1.getId();
        pushApplicationDao.create(pushApplication1);

        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();


        PushApplication pa = pushApplicationDao.find(id);

        assertThat(pa.getId()).isEqualTo(id);

        pushApplication1.setName("Cool Push App 1");
        pushApplicationDao.update(pushApplication1);

        entityManager.flush();
        entityManager.clear();

        pa = pushApplicationDao.find(id);

        assertThat(pa.getName()).isEqualTo("Cool Push App 1");
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

        PushApplication pa = pushApplicationDao.find(id);
        assertThat(pa.getId()).isEqualTo(id);

        AndroidVariant av = new AndroidVariant();
        av.setName("Android Variant");
        av.setGoogleKey("KEY...");
        variantDao.create(av);

        Installation androidInstallation1 = new Installation();
        androidInstallation1.setDeviceToken("12345432122323");
        installationDao.create(androidInstallation1);

        av.getInstallations().add(androidInstallation1);
        variantDao.update(av);

        pa.getAndroidVariants().add(av);
        pushApplicationDao.update(pa);

        assertThat(installationDao.find(androidInstallation1.getId())).isNotNull();

        pushApplicationDao.delete(pa);
        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

        // Installation should be gone:
        assertThat(installationDao.find(androidInstallation1.getId())).isNull();


        // Variant should be gone:
        assertThat(variantDao.find(av.getId())).isNull();

        // PushApp should be gone:
        assertThat(pushApplicationDao.find(id)).isNull();
    }
}
