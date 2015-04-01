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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushApplicationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InstallationDaoBatchTest {

    public static final String DEVICE_TOKEN_1 = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
    public static final String DEVICE_TOKEN_2 = "67890167890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
    private EntityManager entityManager;
    private JPAInstallationDao installationDao;
    private String simplePushVariantID;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        this.createTestData(entityManager);
    }

    private void createTestData(EntityManager entityManager) {

        // create abd configure all the DAOs:
        JPAPushApplicationDao pushApplicationDao = new JPAPushApplicationDao();

        // generic variant DAO:
        JPAVariantDao variantDao = new JPAVariantDao();

        pushApplicationDao.setEntityManager(entityManager);
        variantDao.setEntityManager(entityManager);

        this.installationDao = new JPAInstallationDao();
        this.installationDao.setEntityManager(entityManager);

        // create the PushApplication and a few variants:
        PushApplication pa = new PushApplication();
        pa.setName("PushApplication");
        pushApplicationDao.create(pa);

        AndroidVariant av = new AndroidVariant();
        av.setGoogleKey("Key");
        av.setName("Android");
        av.setDeveloper("me");
        // stash the ID:
        this.androidVariantID = av.getVariantID();
        variantDao.create(av);

        SimplePushVariant sp = new SimplePushVariant();
        sp.setDeveloper("me");
        sp.setName("SimplePush");
        // stash the ID:
        this.simplePushVariantID = sp.getVariantID();
        variantDao.create(sp);

        // register the variants with the Push Application:
        pa.getVariants().add(sp);
        pushApplicationDao.update(pa);

        // ============== SimplePush client installations =========
        for (int i = 0; i < 100; i++) {
            Installation simplePush1 = new Installation();
            simplePush1.setAlias("foo@bar.org");
            simplePush1.setDeviceToken("http://server:8080/update/" + UUID.randomUUID().toString());
            installationDao.create(simplePush1);
            simplePush1.setVariant(sp);
        }

        // register the installation:
        variantDao.update(sp);
    }

    @After
    public void tearDown() {
        try {
            entityManager.getTransaction().commit();
        } catch (RollbackException e) {
            //ignore
        }

        entityManager.close();
    }

    @Test
    public void findPushEndpointsWithoutDeviceType() {
        String tokenFrom, tokenTo;

        List<String> tokensAll = installationDao.findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null, null, null);
        assertEquals(100, tokensAll.size());

        tokenFrom = null;
        tokenTo = tokensAll.get(50);
        List<String> firstBatch = installationDao.findAllDeviceTokenForVariantIDByCriteriaWithLimits(simplePushVariantID, null, null, null, null, null, tokenFrom, tokenTo);
        assertEquals(50, firstBatch.size());
        assertEquals(tokensAll.subList(0, 50), firstBatch);

        tokenFrom = tokensAll.get(50);
        tokenTo = null;
        List<String> secondBatch = installationDao.findAllDeviceTokenForVariantIDByCriteriaWithLimits(simplePushVariantID, null, null, null, null, null, tokenFrom, tokenTo);
        assertEquals(50, secondBatch.size());
        assertEquals(tokensAll.subList(50, 100), secondBatch);

    }
}
