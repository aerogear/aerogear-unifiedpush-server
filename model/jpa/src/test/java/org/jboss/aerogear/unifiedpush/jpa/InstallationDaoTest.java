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
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.ChromePackagedAppVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.WindowsMPNSVariant;
import org.jboss.aerogear.unifiedpush.api.WindowsWNSVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushApplicationDao;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAVariantDao;
import org.jboss.aerogear.unifiedpush.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class InstallationDaoTest {

    public static final String DEVICE_TOKEN_1 = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
    public static final String DEVICE_TOKEN_2 = "67890167890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
    private EntityManager entityManager;
    private JPAInstallationDao installationDao;
    private String androidVariantID;
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
        pa.getVariants().add(av);
        pa.getVariants().add(sp);
        pushApplicationDao.update(pa);

        // ============== Android client installations =========
        Installation android1 = new Installation();
        android1.setAlias("foo@bar.org");
        android1.setDeviceToken(DEVICE_TOKEN_1);
        android1.setDeviceType("Android Phone");
        final Set<Category> categoriesOne = new HashSet<Category>();
        categoriesOne.add(new Category("soccer"));
        android1.setCategories(categoriesOne);

        installationDao.create(android1);

        Installation android2 = new Installation();
        android2.setAlias("foo@bar.org");
        android2.setDeviceToken(DEVICE_TOKEN_2);
        android2.setDeviceType("Android Tablet");
        final Set<Category> categoriesTwo = new HashSet<Category>();
        categoriesTwo.add(new Category("news"));
        android2.setCategories(categoriesTwo);

        installationDao.create(android2);

        // disabled
        Installation android3 = new Installation();
        android3.setAlias("foo@bar.org");
        android3.setDeviceToken("543234234890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
        android3.setDeviceType("Android Tablet");
        android3.setEnabled(false);

        installationDao.create(android3);

        // register them:
        android1.setVariant(av);
        android2.setVariant(av);
        android3.setVariant(av);
        variantDao.update(av);

        // ============== SimplePush client installations =========
        Installation simplePush1 = new Installation();
        simplePush1.setAlias("foo@bar.org");
        simplePush1.setDeviceToken("http://server:8080/update/" + UUID.randomUUID().toString());
        simplePush1.setCategories(categoriesOne);

        installationDao.create(simplePush1);

        Installation simplePush2 = new Installation();
        simplePush2.setAlias("foo@bar.org");
        simplePush2.setDeviceToken("http://server:8080/update/" + UUID.randomUUID().toString());
        simplePush2.setCategories(categoriesTwo);

        installationDao.create(simplePush2);

        Installation simplePush3 = new Installation();
        simplePush3.setAlias("foo@bar.org");
        simplePush3.setDeviceToken("http://server:8080/update/" + UUID.randomUUID().toString());
        simplePush3.setCategories(categoriesTwo);
        simplePush3.setDeviceType("JavaFX Monitor");

        installationDao.create(simplePush3);

        // register the installation:
        simplePush1.setVariant(sp);
        simplePush2.setVariant(sp);
        simplePush3.setVariant(sp);
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
    public void countDevicesForVariants() {
        assertThat(installationDao.getNumberOfDevicesForVariantIDs("me")).isEqualTo(6);
    }

    @Test
    public void findDeviceTokensForOneInstallationOfOneVariant() {
        String[] alias = { "foo@bar.org" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), null);
        assertThat(tokens).hasSize(2);

        Installation one = installationDao.findInstallationForVariantByDeviceToken(androidVariantID, DEVICE_TOKEN_1);
        assertThat(one.getDeviceToken()).isEqualTo(DEVICE_TOKEN_1);

        final Set<String> tokenz = new HashSet<String>();
        tokenz.add(DEVICE_TOKEN_1);
        tokenz.add("foobar223");
        List<Installation> list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertThat(list).hasSize(1);
        assertThat(list).extracting("deviceToken").containsOnly(DEVICE_TOKEN_1);
    }

    @Test
    public void findDeviceTokensForAliasOfVariant() {
        String[] alias = { "foo@bar.org" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), null);
         assertThat(tokens).hasSize(2);
    }

    @Test
    public void findNoDeviceTokensForAliasOfVariant() {
        String[] alias = { "bar@foo.org" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), null);
         assertThat(tokens).hasSize(0);
    }

    @Test
    public void findDeviceTokensForAliasAndDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Tablet" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), Arrays.asList(types));
        assertThat(tokens).hasSize(1);
        assertThat(tokens).containsOnly(DEVICE_TOKEN_2);
    }

    @Test
    public void findNoDeviceTokensForAliasAndUnusedDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Clock" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), Arrays.asList(types));
        assertThat(tokens).isEmpty();
    }

    @Test
    public void findZeroDeviceTokensForAliasAndCategoriesAndDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Tablet" };
        String[] categories = { "soccer" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(categories), Arrays.asList(alias), Arrays
                .asList(types));
        assertThat(tokens).isEmpty();
    }

    @Test
    public void findOneDeviceTokensForAliasAndCategoriesAndDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Phone" };
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(cats), Arrays.asList(alias), Arrays.asList(types));
        assertThat(tokens).hasSize(1);
        assertThat(tokens).containsOnly(DEVICE_TOKEN_1);
    }

    @Test
    public void findTwoDeviceTokensForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(cats), Arrays.asList(alias), null);
        assertThat(tokens).hasSize(2);
    }

    @Test
    public void findTwoDeviceTokensCategories() {
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(cats), null, null);
        assertThat(tokens).hasSize(2);
    }

    @Test
    public void findAndDeleteOneInstallation() {
        final Set<String> tokenz = new HashSet<String>();
        tokenz.add(DEVICE_TOKEN_1);
        tokenz.add("foobar223");
        List<Installation> list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertThat(list).hasSize(1);

        Installation installation = list.get(0);
        assertThat(installation.getDeviceToken()).isEqualTo(DEVICE_TOKEN_1);

        installationDao.delete(installation);

        list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertThat(list).isEmpty();
    }

    @Test
    public void findAndDeleteTwoInstallations() {
        final Set<String> tokenz = new HashSet<String>();
        tokenz.add(DEVICE_TOKEN_1);
        tokenz.add(DEVICE_TOKEN_2);
        List<Installation> list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertThat(list).hasSize(2);

        for (Installation installation : list) {
            installationDao.delete(installation);
        }

        list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
        assertThat(list).hasSize(0);
    }

    @Test
    public void deleteNonExistingInstallation() {
        Installation installation = new Installation();
        installation.setId("2345");

        installationDao.delete(installation);
    }

    @Test
    public void mergeCategories() {
        //given
        final SimplePushVariant variant = new SimplePushVariant();
        entityManager.persist(variant);

        final Installation installation = new Installation();
        installation.setDeviceToken("http://test");
        installation.setCategories(new HashSet<Category>(Arrays.asList(new Category("one"), new Category("two"))));

        final Installation installation2 = new Installation();
        installation2.setDeviceToken("http://test2");
        installation2.setCategories(new HashSet<Category>(Arrays.asList(new Category("one"), new Category("three"))));

        installation.setVariant(variant);
        installation2.setVariant(variant);

        //when
        installationDao.create(installation);

        //then
        final List list = entityManager.createQuery("select c from Category c where c.name = 'one'").getResultList();
        assertThat(list).hasSize(1);
    }

    @Test
    public void findPushEndpointsForAlias() {
        String[] alias = { "foo@bar.org" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null, Arrays.asList(alias), null);
        assertThat(tokens).hasSize(3);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(1)).startsWith("http://server:8080/update/");
    }

    @Test
    public void findZeroPushEndpointsForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] categories = { "US Football" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, Arrays.asList(categories), Arrays.asList(alias), null);
        assertThat(tokens).isEmpty();
    }

    @Test
    public void findOnePushEndpointForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] cats = { "soccer", "weather" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, Arrays.asList(cats), Arrays.asList(alias), null);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");

    }

    @Test
    public void findThreePushEndpointsForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, Arrays.asList(cats), Arrays.asList(alias), null);
        assertThat(tokens).hasSize(3);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(1)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(2)).startsWith("http://server:8080/update/");
    }

    @Test
    public void findThreePushEndpointsForCategories() {
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, Arrays.asList(cats), null, null);
        assertThat(tokens).hasSize(3);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(1)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(2)).startsWith("http://server:8080/update/");
    }

    @Test
    public void findPushEndpointsWithDeviceType() {
        String[] types = {"JavaFX Monitor"};
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null, null, Arrays.asList(types));
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
    }

    @Test
    public void findPushEndpointsWithoutDeviceType() {
        List<String> tokens = installationDao.findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null, null, null);
        assertThat(tokens).hasSize(3);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
    }

    @Test
    public void shouldValidateDeviceId() {
        // given
        final Installation installation = new Installation();
        installation.setDeviceToken("invalid");

        final iOSVariant variant = new iOSVariant();
        variant.setPassphrase("12");
        variant.setCertificate("12".getBytes());
        entityManager.persist(variant);
        installation.setVariant(variant);

        // when
        installationDao.create(installation);
        try {
            entityManager.flush();
            fail("ConstraintViolationException should have been thrown");
        } catch (ConstraintViolationException violationException) {
            // then
            final Set<ConstraintViolation<?>> constraintViolations = violationException.getConstraintViolations();
            assertThat(constraintViolations).isNotEmpty();
            assertThat(constraintViolations.size()).isEqualTo(1);

            assertThat(constraintViolations.iterator().next().getMessage()).isEqualTo(
                    "Device token is not valid for this device type");
        }
    }

    @Test
    public void shouldSaveWhenValidateDeviceIdIOS() {
        // given
        final Installation installation = new Installation();
        installation.setDeviceToken("1ce51dad49a77ca7b45924074bcc4f19aea20378f5feda202fbba3beed7073d7");
        final iOSVariant variant = new iOSVariant();
        variant.setPassphrase("12");
        variant.setCertificate("12".getBytes());

        // when
        deviceTokenTest(installation, variant);
    }

    @Test
    public void shouldSaveWhenValidateDeviceIdWindows() {
        // given
        final Installation installation = new Installation();
        installation.setDeviceToken("https://db3.notify.windows.com/?token=AgYAAACH%2fZixlZK4v%2bkD3LFiz7zHOJm13"
                + "smBVRn8rH%2b32Xu6tv3fj%2fh8bb4VhNTS7NqS8TclpW044YxAbaN%2bB4NjpyVSZs3He7SwwjExbEsBFRLYc824%2f0"
                + "615fPox8bwoxrTU%3d");
        
        final WindowsWNSVariant variant = new WindowsWNSVariant();
        variant.setClientSecret("12");
        variant.setSid("12");

        // when
        deviceTokenTest(installation, variant);
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldNotSaveWhenSimplePushTokenInvalid() {
        // given
        final Installation installation = new Installation();
        installation.setDeviceToken("htp://invalid");

        final SimplePushVariant variant = new SimplePushVariant();

        // when
        deviceTokenTest(installation, variant);
    }

    @Test
    public void shouldSaveWhenValidateDeviceIdMPNSWindows() {
        // given
        final Installation installation = new Installation();
        installation.setDeviceToken("http://s.notify.live.net/u/1/db3/HmQAAACsY7ZBMnNW6QnfPcHXC1gwvHFlPeujLy"
                + "aLyoJmTm79gofALwJGBefhxH_Rjpz4oAoK5O5zL2nQwaFZpLMpXUP/d2luZG93c3Bob25lZGVmYXVsdA/AGVGhYlaBG"
                + "GphX2C8gGmg/vedAL_DKqnF00b4O3NCIifacDEQ");

        WindowsMPNSVariant variant = new WindowsMPNSVariant();
        
        // when
        deviceTokenTest(installation, variant);
    }
    

    @Test
    public void shouldSaveWhenValidChromeApp() {
        // given
        final Installation installation = new Installation();
        installation.setDeviceToken(DEVICE_TOKEN_1);

        final ChromePackagedAppVariant variant = new ChromePackagedAppVariant();
        variant.setClientId("12");
        variant.setClientSecret("12");
        variant.setRefreshToken("12");

        //when
        deviceTokenTest(installation, variant);
    }

    @Test
    public void shouldSaveWhenSimplePushTokenValid() {
        // given
        final Installation installation = new Installation();
        installation.setDeviceToken("http://valid/but/you/should/use/https");

        final SimplePushVariant variant = new SimplePushVariant();

        // when
        deviceTokenTest(installation, variant);
    }

    @Test
    public void shouldSaveWhenValidateDeviceIdAndroid() {
        // given
        final Installation installation = new Installation();
        installation.setDeviceToken("APA91bHpbMXepp4odlb20vYOv0gQyNIyFu2X3OXR3TjqR8qecgWivima_UiLPFgUBs_10Nys2TUwUy"
                + "WlixrIta35NXW-5Z85OdXcbb_3s3p0qaa_a7NpFlaX9GpidK_BdQNMsx2gX8BrE4Uw7s22nPCcEn1U1_mo-"
                + "T6hcF5unYt965PDwRTRss8");

        final AndroidVariant variant = new AndroidVariant();
        variant.setGoogleKey("12");
        variant.setProjectNumber("12");

        // when
        deviceTokenTest(installation, variant);
    }

    private void deviceTokenTest(Installation installation, Variant variant) {
        entityManager.persist(variant);
        installation.setVariant(variant);

        // when
        installationDao.create(installation);
        entityManager.flush();
    }

    @Test
    public void primaryKeyUnmodifiedAfterUpdate() {
        Installation android1 = new Installation();
        android1.setAlias("foo@bar.org");
        android1.setDeviceToken(DEVICE_TOKEN_1);
        android1.setDeviceType("Android Phone");
        final Set<Category> categoriesOne = new HashSet<Category>();
        final Category category = entityManager.createQuery("from Category where name = :name", Category.class)
                .setParameter("name", "soccer").getSingleResult();
        categoriesOne.add(category);
        android1.setCategories(categoriesOne);
        final String id = android1.getId();

        final AndroidVariant variant = new AndroidVariant();
        variant.setGoogleKey("12");
        variant.setProjectNumber("12");
        entityManager.persist(variant);
        android1.setVariant(variant);

        installationDao.create(android1);

        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();

        Installation installation = installationDao.find(id);

        assertThat(installation.getId()).isEqualTo(id);
        assertThat(installation.getDeviceType()).isEqualTo("Android Phone");

        final String alias = "foobar@bar.org";
        android1.setAlias(alias);
        installationDao.update(android1);
        entityManager.flush();
        entityManager.clear();

        installation = installationDao.find(id);

        assertThat(installation.getAlias()).isEqualTo(alias);
    }

    @Test
    public void shouldSelectInstallationsByVariantForDeveloper() {
        //given
        String developer = "me";

        //when
        final PageResult pageResult = installationDao.findInstallationsByVariantForDeveloper(androidVariantID, developer, 0, 1);

        //then
        assertThat(pageResult).isNotNull();
        assertThat(pageResult.getResultList()).isNotEmpty().hasSize(1);
        assertThat(pageResult.getCount()).isEqualTo(3);
    }

    @Test
    public void shouldSelectInstallationsByVariant() {
        //given
        String developer = "me";

        //when
        final PageResult pageResult = installationDao.findInstallationsByVariant(androidVariantID, 0, 1);

        //then
        assertThat(pageResult).isNotNull();
        assertThat(pageResult.getResultList()).isNotEmpty().hasSize(1);
        assertThat(pageResult.getCount()).isEqualTo(3);
    }

    @Test(expected = PersistenceException.class)
    public void testTooLongDeviceToken() {
        AndroidVariant variant = new AndroidVariant();
        variant.setGoogleKey("123");
        variant.setProjectNumber("123");

        entityManager.persist(variant);

        Installation android1 = new Installation();
        android1.setAlias("foo@bar.org");
        android1.setDeviceToken(TestUtils.longString(4097));
        android1.setVariant(variant);

        installationDao.create(android1);

        entityManager.flush();
    }

    @Test
    public void testLongDeviceToken() {
        AndroidVariant variant = new AndroidVariant();
        variant.setGoogleKey("123");
        variant.setProjectNumber("123");

        entityManager.persist(variant);

        Installation android1 = new Installation();
        android1.setAlias("foo@bar.org");
        android1.setDeviceToken(TestUtils.longString(4096));
        android1.setVariant(variant);

        installationDao.create(android1);

        entityManager.flush();
    }
}
