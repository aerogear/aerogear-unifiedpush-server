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

import org.jboss.aerogear.unifiedpush.api.APNsVariant;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper("Admin")).isEqualTo(0);

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

        assertThat(pushApplicationDao.findAllForDeveloper("Admin", 0, 10).getResultList()).hasSize(2);
        assertThat(pushApplicationDao.findAllForDeveloper("Dave The Drummer", 0, 10).getResultList()).hasSize(1);
        assertThat(pushApplicationDao.findAllForDeveloper("Dave The Drummer", 0, 10).getResultList()).extracting("name").containsOnly("Push App 3");
        assertThat(pushApplicationDao.findAllForDeveloper("Admin The Drummer", 0, 10).getResultList()).isEmpty();

        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper("Dave The Drummer")).isEqualTo(1);
        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper("Admin")).isEqualTo(2);

        // check all:
        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper()).isEqualTo(3);
        assertThat(pushApplicationDao.findAll(0, 10).getCount()).isEqualTo(3);

        // check exact:
        assertThat(pushApplicationDao.findAllByPushApplicationID(pushApplication2.getPushApplicationID()).getName()).isEqualTo("Push App 2");
        assertThat(pushApplicationDao.findAllByPushApplicationID(pushApplication2.getPushApplicationID()).getName()).isNotEqualTo("Push App 3");

    }

    @Test
    public void findAllIDsForDeveloper() {

        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper("Admin")).isEqualTo(0);

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

        assertThat(pushApplicationDao.findAllPushApplicationIDsForDeveloper("Admin")).hasSize(2);
        assertThat(pushApplicationDao.findAllPushApplicationIDsForDeveloper("Dave The Drummer")).hasSize(1);
        assertThat(pushApplicationDao.findAllPushApplicationIDsForDeveloper("Admin The Drummer")).isEmpty();

        // check all:
        assertThat(pushApplicationDao.getNumberOfPushApplicationsForDeveloper()).isEqualTo(3);
        assertThat(pushApplicationDao.findAll(0, 10).getCount()).isEqualTo(3);
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
        androidInstallation1.setDeviceToken("1234543212232312345432122323123454321223231234543212232312345432122323123454321223231234543212232312345432122323");
        installationDao.create(androidInstallation1);

        androidInstallation1.setVariant(av);
        variantDao.update(av);

        pa.getVariants().add(av);
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

    @Test
    public void shouldCountInstallations() {
        PushApplication pushApplication1 = new PushApplication();
        pushApplication1.setName("Push App 1");
        final String id = pushApplication1.getId();
        pushApplicationDao.create(pushApplication1);

        PushApplication pa = pushApplicationDao.find(id);

        AndroidVariant av = new AndroidVariant();
        av.setName("Android Variant");
        av.setGoogleKey("KEY...");
        variantDao.create(av);

        APNsVariant ios = new APNsVariant();
        ios.setName("spelling is hard");
        ios.setPassphrase("123");
        ios.setCertificate("12".getBytes());
        variantDao.create(ios);

        Installation androidInstallation1 = new Installation();
        androidInstallation1.setDeviceToken("CSPA91bGDWDdlxW3EmSs2bH7Qlo5AOfbCJtmyOukYxVHq8KKUqpPLBLUjettGYoN2nahBbAe3GgmxKPcZnqEIFFxHw3brKOSmeXjZQuEVehSJTUdJuXUCmR3XweZ2MM455fYMcvkUse1DIp1wjxnik2uHYSNl87wrJzLddoC7tPpgch3eJAf");
        installationDao.create(androidInstallation1);

        Installation androidInstallation2 = new Installation();
        androidInstallation2.setDeviceToken("ASPA91bGDWDdlxW3EmSs2bH7Qlo5AOfbCJtmyOukYxVHq8KKUqpPLBLUjettGYoN2nahBbAe3GgmxKPcZnqEIFFxHw3brKOSmeXjZQuEVehSJTUdJuXUCmR3XweZ2MM455fYMcvkUse1DIp1wjxnik2uHYSNl87wrJzLddoC7tPpgch3eJAf");
        installationDao.create(androidInstallation2);

        Installation iosInstallation1 = new Installation();
        iosInstallation1.setDeviceToken("33ee51dad49a77ca7b45924074bcc4f19aea20308f5feda202fbba3baed7073d7");
        installationDao.create(iosInstallation1);

        androidInstallation1.setVariant(av);
        androidInstallation2.setVariant(av);
        iosInstallation1.setVariant(ios);
        variantDao.update(av);
        variantDao.update(ios);

        pa.getVariants().add(av);
        pa.getVariants().add(ios);
        pushApplicationDao.update(pa);

        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

        final Map<String, Long> result = pushApplicationDao.countInstallationsByType(pushApplication1.getPushApplicationID());

        System.out.println("result = " + result);
        assertThat(result).isNotEmpty();
        assertThat(result.get(av.getVariantID())).isEqualTo(2L);
        assertThat(result.get(ios.getVariantID())).isEqualTo(1L);
    }

    @Test
    public void shouldFindPushApplicationNameAndIDbasedOnVariantID() {
        //given
        PushApplication pushApplication1 = new PushApplication();
        final String appName = "Push App 1";
        pushApplication1.setName(appName);
        final String id = pushApplication1.getId();
        pushApplicationDao.create(pushApplication1);

        PushApplication pa = pushApplicationDao.find(id);

        AndroidVariant av = new AndroidVariant();
        av.setName("Android Variant");
        av.setGoogleKey("KEY...");
        variantDao.create(av);

        AndroidVariant ignored = new AndroidVariant();
        ignored.setName("ignored");
        ignored.setGoogleKey("123");
        variantDao.create(ignored);

        APNsVariant APNsVariant = new APNsVariant();
        APNsVariant.setName("ignored");
        APNsVariant.setCertificate(new byte[1]);
        APNsVariant.setPassphrase("123");
        variantDao.create(APNsVariant);

        pa.getVariants().add(av);
        pa.getVariants().add(ignored);
        pa.getVariants().add(APNsVariant);
        pushApplicationDao.update(pa);

        entityManager.flush();
        entityManager.clear();

        //when
        final List<PushApplication> applications = pushApplicationDao.findByVariantIds(Arrays.asList(av.getVariantID()));

        //then
        assertThat(applications).isNotEmpty();
        assertThat(applications.size()).isEqualTo(1);

        final PushApplication application = applications.iterator().next();
        assertThat(application.getName()).isEqualTo(appName);
        assertThat(application.getVariants()).isNotEmpty();
        assertThat(application.getVariants().size()).isEqualTo(1);
        assertThat(application.getVariants().iterator().next().getId()).isEqualTo(av.getId());
    }
}
