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

import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.*;

public class InstallationDaoTest {

    private EntityManager em;
    private InstallationDaoImpl installationDao;
    private AndroidVariantDaoImpl variantDao;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        em = emf.createEntityManager();

        installationDao = new InstallationDaoImpl();
        installationDao.setEntityManager(em);

        variantDao = new AndroidVariantDaoImpl();
        variantDao.setEntityManager(em);

        // start the shindig
        em.getTransaction().begin();
    }

    @After
    public void tearDown() {
        em.getTransaction().commit();

        em.close();
    }

    @Test
    public void saveInstallation() {

        InstallationImpl installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceToken("123456");

        InstallationImpl stored = installationDao.create(installation);

        assertEquals("Alias should be same", stored.getAlias(), installation.getAlias());
        assertNotNull(stored.getId());
    }

    @Test
    public void findDeviceTokenForOneInstallation() {

        // create and save variant:
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setVariantID(UUID.randomUUID().toString());
        androidVariant.setGoogleKey("Key");
        variantDao.create(androidVariant);

        // create and save installation:
        InstallationImpl installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceToken("123456");
        installationDao.create(installation);

        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        String[] alias = {"foo@bar.org"};
        List<String> tokens =   installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, Arrays.asList(alias), null);
        assertEquals(1, tokens.size());
        assertEquals("123456", tokens.get(0));
    }

    @Test
    public void findDeviceTokenForMultipleInstallations() {

        // create and save variant:
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setVariantID(UUID.randomUUID().toString());
        androidVariant.setGoogleKey("Key");
        variantDao.create(androidVariant);

        for (int i=0;i<10; i++) {

            // create and save installation:
            InstallationImpl installation = new InstallationImpl();
            installation.setAlias("foo@bar.org");
            installation.setDeviceToken(UUID.randomUUID().toString());
            installationDao.create(installation);

            // register the installation:
            androidVariant.getInstallations().add(installation);
            variantDao.update(androidVariant);
        }

        String[] alias = {"foo@bar.org"};
        List<String> tokens =   installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, Arrays.asList(alias), null);
        assertEquals(10, tokens.size());
    }


    @Test
    public void findDeviceTokensForAlias() {

        // create and save variant:
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setVariantID(UUID.randomUUID().toString());
        androidVariant.setGoogleKey("Key");
        variantDao.create(androidVariant);

        // create and save installation:
        InstallationImpl installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Phone");
        installation.setDeviceToken("123456");
        installationDao.create(installation);
        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        // a different
        installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Tablet");
        installation.setDeviceToken("678901");
        installationDao.create(installation);

        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        String[] alias = {"foo@bar.org"};
        List<String> tokens =   installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, Arrays.asList(alias), null);
        assertEquals(2, tokens.size());
    }

    @Test
    public void findEnabledDeviceTokensForAlias() {

        // create and save variant:
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setVariantID(UUID.randomUUID().toString());
        androidVariant.setGoogleKey("Key");
        variantDao.create(androidVariant);

        // create and save installation:
        InstallationImpl installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Phone");
        installation.setDeviceToken("123456");
        installationDao.create(installation);
        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        // a different
        installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Tablet");
        installation.setDeviceToken("678901");
        installation.setEnabled(false);
        installationDao.create(installation);

        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        String[] alias = {"foo@bar.org"};
        List<String> tokens =   installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, Arrays.asList(alias), null);
        assertEquals(1, tokens.size());
        assertEquals("123456", tokens.get(0));

    }

    @Test
    public void findDeviceTokensForAliasAndDeviceType() {

        // create and save variant:
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setVariantID(UUID.randomUUID().toString());
        androidVariant.setGoogleKey("Key");
        variantDao.create(androidVariant);

        // create and save installation:
        InstallationImpl installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Phone");
        installation.setDeviceToken("123456");
        installationDao.create(installation);
        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        // a different
        installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Tablet");
        installation.setDeviceToken("678901");
        installationDao.create(installation);

        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        String[] alias = {"foo@bar.org"};
        String[] types = {"Android Tablet"};
        List<String> tokens =   installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), null, Arrays.asList(alias), Arrays.asList(types));
        assertEquals(1, tokens.size());
        assertEquals("678901", tokens.get(0));

    }

    @Test
    public void findZeroDeviceTokensForAliasAndCategoriesAndDeviceType() {

        // create and save variant:
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setVariantID(UUID.randomUUID().toString());
        androidVariant.setGoogleKey("Key");
        variantDao.create(androidVariant);

        // create and save installation:
        InstallationImpl installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Phone");
        installation.setDeviceToken("123456");
        installationDao.create(installation);
        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        // a different
        installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Tablet");
        installation.setDeviceToken("678901");
        installationDao.create(installation);

        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        String[] alias =     {"foo@bar.org"};
        String[] types =     {"Android Tablet"};
        String[] categories = {"soccer"};
        List<String> tokens =   installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList(categories), Arrays.asList(alias), Arrays.asList(types));
        assertEquals(0, tokens.size());
    }


    @Test
    public void findOneDeviceTokensForAliasAndCategoriesAndDeviceType() {

        // create and save variant:
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setVariantID(UUID.randomUUID().toString());
        androidVariant.setGoogleKey("Key");
        variantDao.create(androidVariant);

        // create and save installation:
        InstallationImpl installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Phone");
        installation.setDeviceToken("123456");

        final Set<String> categories = new HashSet<String>();
        categories.add("soccer");
        installation.setCategories(categories);

        installationDao.create(installation);
        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        // a different
        installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Tablet");
        installation.setDeviceToken("678901");
        installationDao.create(installation);

        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        String[] alias = {"foo@bar.org"};
        String[] types = {"Android Phone"};
        String[] cats  = {"soccer", "news", "weather"};
        List<String> tokens =   installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList(cats), Arrays.asList(alias), Arrays.asList(types));
        assertEquals(1, tokens.size());
        assertEquals("123456", tokens.get(0));

    }

    @Test
    public void findTwoDeviceTokensForAliasAndCategoriesAndDeviceType() {

        // create and save variant:
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setVariantID(UUID.randomUUID().toString());
        androidVariant.setGoogleKey("Key");
        variantDao.create(androidVariant);

        // create and save installation:
        InstallationImpl installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Phone");
        installation.setDeviceToken("123456");

        Set<String> categories = new HashSet<String>();
        categories.add("soccer");
        installation.setCategories(categories);

        installationDao.create(installation);
        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        // a different
        installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Tablet");

        categories = new HashSet<String>();
        categories.add("news");
        installation.setCategories(categories);


        installation.setDeviceToken("678901");
        installationDao.create(installation);

        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        String[] alias = {"foo@bar.org"};
        String[] types = {"Android Phone"};
        String[] cats  = {"soccer", "news", "weather"};
        List<String> tokens =   installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList(cats), Arrays.asList(alias), Arrays.asList(types));
        assertEquals(2, tokens.size());

    }

    @Test
    public void findTwoDeviceTokensCategories() {

        // create and save variant:
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setVariantID(UUID.randomUUID().toString());
        androidVariant.setGoogleKey("Key");
        variantDao.create(androidVariant);

        // create and save installation:
        InstallationImpl installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Phone");
        installation.setDeviceToken("123456");

        Set<String> categories = new HashSet<String>();
        categories.add("soccer");
        installation.setCategories(categories);

        installationDao.create(installation);
        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        // a different
        installation = new InstallationImpl();
        installation.setAlias("foo@bar.org");
        installation.setDeviceType("Android Tablet");

        categories = new HashSet<String>();
        categories.add("news");
        installation.setCategories(categories);


        installation.setDeviceToken("678901");
        installationDao.create(installation);

        // register the installation:
        androidVariant.getInstallations().add(installation);
        variantDao.update(androidVariant);

        String[] cats  = {"soccer", "news", "weather"};
        List<String> tokens =   installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), Arrays.asList(cats), null, null);
        assertEquals(2, tokens.size());

    }
}

