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

import net.jakubholy.dbunitexpress.EmbeddedDbTesterRule;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.WindowsMPNSVariant;
import org.jboss.aerogear.unifiedpush.api.WindowsWNSVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.api.AdmVariant;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAInstallationDao;
import org.jboss.aerogear.unifiedpush.utils.DaoDeployment;
import org.jboss.aerogear.unifiedpush.utils.TestUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class InstallationDaoTest {

    public static final String DEVICE_TOKEN_1 = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
    public static final String DEVICE_TOKEN_2 = "67890167890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

    @Inject
    private EntityManager entityManager;
    @Inject
    private JPAInstallationDao installationDao;

    private String androidVariantID = "1";
    private String simplePushVariantID = "2";

    @Deployment
    public static JavaArchive createDeployment() {
        return DaoDeployment.createDeployment();
    }

    @Rule
    public EmbeddedDbTesterRule testDb = new EmbeddedDbTesterRule("Installations.xml");

    @Before
    public void setUp() {
        entityManager.getTransaction().begin();
    }

    @After
    public void tearDown() {
        entityManager.getTransaction().rollback();
    }

    @Test
    public void countDevicesForVariants() {
        assertThat(installationDao.getNumberOfDevicesForVariantIDs("me")).isEqualTo(6);
    }

    @Test
    public void findDeviceTokensForOneInstallationOfOneVariant() {
        String[] alias = { "foo@bar.org" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), null);
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
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), null);
         assertThat(tokens).hasSize(2);
    }

    @Test
    public void findNoDeviceTokensForAliasOfVariant() {
        String[] alias = { "bar@foo.org" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), null);
         assertThat(tokens).hasSize(0);
    }

    @Test
    public void findDeviceTokensForAliasAndDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Tablet" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), Arrays.asList(types));
        assertThat(tokens).hasSize(1);
        assertThat(tokens).containsOnly(DEVICE_TOKEN_2);
    }

    @Test
    public void findNoDeviceTokensForAliasAndUnusedDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Clock" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, Arrays.asList(alias), Arrays.asList(types));
        assertThat(tokens).isEmpty();
    }

    @Test
    public void findZeroDeviceTokensForAliasAndCategoriesAndDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Tablet" };
        String[] categories = { "soccer" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(categories), Arrays.asList(alias), Arrays
                .asList(types));
        assertThat(tokens).isEmpty();
    }

    @Test
    public void findOneDeviceTokensForAliasAndCategoriesAndDeviceType() {
        String[] alias = { "foo@bar.org" };
        String[] types = { "Android Phone" };
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(cats), Arrays.asList(alias), Arrays.asList(types));
        assertThat(tokens).hasSize(1);
        assertThat(tokens).containsOnly(DEVICE_TOKEN_1);
    }

    @Test
    public void findTwoDeviceTokensForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(cats), Arrays.asList(alias), null);
        assertThat(tokens).hasSize(2);
    }

    @Test
    public void findTwoDeviceTokensCategories() {
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, Arrays.asList(cats), null, null);
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
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null, Arrays.asList(alias), null);
        assertThat(tokens).hasSize(3);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(1)).startsWith("http://server:8080/update/");
    }

    @Test
    public void findZeroPushEndpointsForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] categories = { "US Football" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, Arrays.asList(categories), Arrays.asList(alias), null);
        assertThat(tokens).isEmpty();
    }

    @Test
    public void findOnePushEndpointForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] cats = { "soccer", "weather" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, Arrays.asList(cats), Arrays.asList(alias), null);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");

    }

    @Test
    public void findThreePushEndpointsForAliasAndCategories() {
        String[] alias = { "foo@bar.org" };
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, Arrays.asList(cats), Arrays.asList(alias), null);
        assertThat(tokens).hasSize(3);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(1)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(2)).startsWith("http://server:8080/update/");
    }

    @Test
    public void findThreePushEndpointsForCategories() {
        String[] cats = { "soccer", "news", "weather" };
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, Arrays.asList(cats), null, null);
        assertThat(tokens).hasSize(3);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(1)).startsWith("http://server:8080/update/");
        assertThat(tokens.get(2)).startsWith("http://server:8080/update/");
    }

    @Test
    public void findPushEndpointsWithDeviceType() {
        String[] types = {"JavaFX Monitor"};
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null, null, Arrays.asList(types));
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
    }

    @Test
    public void findPushEndpointsWithoutDeviceType() {
        List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null, null, null);
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

    @Test
    public void shouldSaveWhenValidateDeviceIdAdm() {
        // given
        final Installation installation = new Installation();
        installation.setDeviceToken("amzn1.adm-registration.v3.Y29tLmFtYXpvbi5EZXZpY2VNZXNzYWdpbmcuUmVnaXN0cmF0a" +
                "W9uSWRFbmNyeXB0aW9uS2V5ITEhWTlLSFlBZDlOSU12cTUzdlpIQzZJd3VZVk9CZ0g1bUdWUkJrL0hOTkZ5UGFPN1FxY3pP" +
                "WXJVL0laWGdrczVKU1MwSG8rVDUva2hkS3h5WjE4YUZHM3NoTXpOMUxCa2tORDdsY2FxemVxcG5lWXR1eC9UeHZMTWVScUY" +
                "wT3JwUXFzZFFCMi9vaHhmQjk2dERwK29JNEtFTm1TRGhLMFhnd0FPT3FPWGRwMi9GQllNSmN5TVh4YlZ4VlNQdVcvbHEveU" +
                "JkZExoMTdrZnNaVWpOMGlVMTBDbndkNERSd3Z4VjlpVm9hUy9mTXhLdUsxSVV5cjY1cngrQWYwdjN4WGxvWWJGL3ZDNXF6T" +
                "2FPa0JTL3Z6bGtxUUFUN3h4bXg1YitBTHlpbGkxazdJbHBIVm1PUm0rUkgveDFOdzFDQUVhQ1BXcE1Ud3ZpY2ROcUxGWlRt" +
                "VFM2bml3PT0hQVcwQ2puM3g2THgvaWJ0cE9nMzBEUT09");

        final AdmVariant variant = new AdmVariant();
        variant.setClientSecret("12");
        variant.setClientId("12");

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
        installation.setDeviceToken("https://s.notify.live.net/u/1/db3/HmQAAACsY7ZBMnNW6QnfPcHXC1gwvHFlPeujLy"
                + "aLyoJmTm79gofALwJGBefhxH_Rjpz4oAoK5O5zL2nQwaFZpLMpXUP/d2luZG93c3Bob25lZGVmYXVsdA/AGVGhYlaBG"
                + "GphX2C8gGmg/vedAL_DKqnF00b4O3NCIifacDEQ");

        WindowsMPNSVariant variant = new WindowsMPNSVariant();

        // when
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

    private List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID, List<String> categories, List<String> aliases, List<String> deviceTypes) {
        try {
            ResultsStream<String> tokenStream = installationDao.findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes, Integer.MAX_VALUE, null).executeQuery();
            List<String> list = new ArrayList<String>();
            while (tokenStream.next()) {
                list.add(tokenStream.get());
            }
            return list;
        } catch (ResultStreamException e) {
            throw new IllegalStateException(e);
        }
    }
}
