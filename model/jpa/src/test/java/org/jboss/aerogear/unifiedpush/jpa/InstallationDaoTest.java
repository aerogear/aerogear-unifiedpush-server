/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.jboss.aerogear.unifiedpush.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import net.jakubholy.dbunitexpress.EmbeddedDbTesterRule;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.dto.Count;
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

@RunWith(Arquillian.class)
public class InstallationDaoTest {

  public static final String DEVICE_TOKEN_1 = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
  public static final String DEVICE_TOKEN_2 = "67890167890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
  public static final String DEVICE_TOKEN_3 = "27890167890:123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
  public static final String DEVICE_TOKEN_4 = "12345678901:23456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";

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
  public void countDevicesForLoginName() {
    assertThat(installationDao.getNumberOfDevicesForLoginName("me")).isEqualTo(9);
  }

  @Test
  public void testTotalNumberOfDevices() {
    assertThat(installationDao.getTotalNumberOfDevices()).isEqualTo(10);
  }

  @Test
  public void getNumberOfDevicesForVariantID() {
    assertThat(installationDao.getNumberOfDevicesForVariantID("1")).isEqualTo(6);
    assertThat(installationDao.getNumberOfDevicesForVariantID("2")).isEqualTo(3);
  }

  @Test
  public void findDeviceTokensForOneInstallationOfOneVariant() {
    String[] alias = {"foo@bar.org"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null,
        Arrays.asList(alias), null);
    assertThat(tokens).hasSize(4);

    Installation one = installationDao
        .findInstallationForVariantByDeviceToken(androidVariantID, DEVICE_TOKEN_1);
    assertThat(one.getDeviceToken()).isEqualTo(DEVICE_TOKEN_1);

    final Set<String> tokenz = new HashSet<>();
    tokenz.add(DEVICE_TOKEN_1);
    tokenz.add("foobar223");
    List<Installation> list = installationDao
        .findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
    assertThat(list).hasSize(1);
    assertThat(list).extracting("deviceToken").containsOnly(DEVICE_TOKEN_1);
  }

  @Test
  public void findDeviceTokensOfVariant() {
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null, null,
        null);
    assertThat(tokens).hasSize(4);
    assertThat(tokens).containsOnly(DEVICE_TOKEN_1, DEVICE_TOKEN_2, DEVICE_TOKEN_3, DEVICE_TOKEN_4);
  }

  @Test
  public void findOldGCMDeviceTokensOfVariant() {
    List<String> tokens = findAllOldGCMDeviceTokenForVariantIDByCriteria(androidVariantID, null,
        null, null);
    assertThat(tokens).hasSize(2);
    assertThat(tokens).containsOnly(DEVICE_TOKEN_1, DEVICE_TOKEN_2);
  }

  @Test
  public void findDeviceTokensForAliasOfVariant() {
    String[] alias = {"foo@bar.org"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null,
        Arrays.asList(alias), null);
    assertThat(tokens).hasSize(4);
    assertThat(tokens).containsOnly(DEVICE_TOKEN_1, DEVICE_TOKEN_2, DEVICE_TOKEN_3, DEVICE_TOKEN_4);
  }

  @Test
  public void findNoDeviceTokensForAliasOfVariant() {
    String[] alias = {"bar@foo.org"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null,
        Arrays.asList(alias), null);
    assertThat(tokens).hasSize(0);
  }

  @Test
  public void findDeviceTokensForAliasAndDeviceType() {
    String[] alias = {"foo@bar.org"};
    String[] types = {"Android Tablet"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null,
        Arrays.asList(alias), Arrays.asList(types));
    assertThat(tokens).hasSize(2);
    assertThat(tokens).containsOnly(DEVICE_TOKEN_2, DEVICE_TOKEN_3);
  }

  @Test
  public void findNoDeviceTokensForAliasAndUnusedDeviceType() {
    String[] alias = {"foo@bar.org"};
    String[] types = {"Android Clock"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID, null,
        Arrays.asList(alias), Arrays.asList(types));
    assertThat(tokens).isEmpty();
  }

  @Test
  public void findZeroDeviceTokensForAliasAndCategoriesAndDeviceType() {
    String[] alias = {"foo@bar.org"};
    String[] types = {"Android Tablet"};
    String[] categories = {"soccer"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID,
        Arrays.asList(categories), Arrays.asList(alias), Arrays
            .asList(types));
    assertThat(tokens).isEmpty();
  }

  @Test
  public void findOneDeviceTokensForAliasAndCategoriesAndDeviceType() {
    String[] alias = {"foo@bar.org"};
    String[] types = {"Android Phone"};
    String[] cats = {"soccer", "news", "weather"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID,
        Arrays.asList(cats), Arrays.asList(alias), Arrays.asList(types));
    assertThat(tokens).hasSize(1);
    assertThat(tokens).containsOnly(DEVICE_TOKEN_1);
  }

  @Test
  public void findTwoDeviceTokensForAliasAndCategories() {
    String[] alias = {"foo@bar.org"};
    String[] cats = {"soccer", "news", "weather"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID,
        Arrays.asList(cats), Arrays.asList(alias), null);
    assertThat(tokens).hasSize(2);
  }

  @Test
  public void findTwoDeviceTokensCategories() {
    String[] cats = {"soccer", "news", "weather"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(androidVariantID,
        Arrays.asList(cats), null, null);
    assertThat(tokens).hasSize(2);
  }

  @Test
  public void findAndDeleteOneInstallation() {
    final Set<String> tokenz = new HashSet<>();
    tokenz.add(DEVICE_TOKEN_1);
    tokenz.add("foobar223");
    List<Installation> list = installationDao
        .findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
    assertThat(list).hasSize(1);

    Installation installation = list.get(0);
    assertThat(installation.getDeviceToken()).isEqualTo(DEVICE_TOKEN_1);

    installationDao.delete(installation);

    list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
    assertThat(list).isEmpty();
  }

  @Test
  public void findAndDeleteTwoInstallations() {
    final Set<String> tokenz = new HashSet<>();
    tokenz.add(DEVICE_TOKEN_1);
    tokenz.add(DEVICE_TOKEN_2);
    List<Installation> list = installationDao
        .findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
    assertThat(list).hasSize(2);

    list.forEach(installation -> {
      installationDao.delete(installation);
    });

    list = installationDao.findInstallationsForVariantByDeviceTokens(androidVariantID, tokenz);
    assertThat(list).hasSize(0);
  }

  @Test(expected = EntityNotFoundException.class)
  public void deleteNonExistingInstallation() {
    Installation installation = new Installation();
    installation.setId("2345");

    installationDao.delete(installation);
  }

  @Test
  public void mergeCategories() {
    //given
    final AndroidVariant variant = new AndroidVariant();
    variant.setName("Android Variant Name");
    variant.setGoogleKey("12");
    variant.setProjectNumber("12");
    entityManager.persist(variant);

    final Installation installation = new Installation();
    installation.setDeviceToken(DEVICE_TOKEN_1);

    installation
        .setCategories(new HashSet<>(Arrays.asList(new Category("one"), new Category("two"))));

    final Installation installation2 = new Installation();
    installation2.setDeviceToken(DEVICE_TOKEN_2);
    installation2
        .setCategories(new HashSet<>(Arrays.asList(new Category("one"), new Category("three"))));

    installation.setVariant(variant);
    installation2.setVariant(variant);

    //when
    installationDao.create(installation);

    //then
    final List list = entityManager.createQuery("select c from Category c where c.name = 'one'")
        .getResultList();
    assertThat(list).hasSize(1);
  }

  @Test
  public void findPushEndpointsForAlias() {
    String[] alias = {"foo@bar.org"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null,
        Arrays.asList(alias), null);
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
    assertThat(tokens.get(1)).startsWith("http://server:8080/update/");
  }

  @Test
  public void findZeroPushEndpointsForAliasAndCategories() {
    String[] alias = {"foo@bar.org"};
    String[] categories = {"US Football"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID,
        Arrays.asList(categories), Arrays.asList(alias), null);
    assertThat(tokens).isEmpty();
  }

  @Test
  public void findOnePushEndpointForAliasAndCategories() {
    String[] alias = {"foo@bar.org"};
    String[] cats = {"soccer", "weather"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID,
        Arrays.asList(cats), Arrays.asList(alias), null);
    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0)).startsWith("http://server:8080/update/");

  }

  @Test
  public void findThreePushEndpointsForAliasAndCategories() {
    String[] alias = {"foo@bar.org"};
    String[] cats = {"soccer", "news", "weather"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID,
        Arrays.asList(cats), Arrays.asList(alias), null);
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
    assertThat(tokens.get(1)).startsWith("http://server:8080/update/");
    assertThat(tokens.get(2)).startsWith("http://server:8080/update/");
  }

  @Test
  public void findThreePushEndpointsForCategories() {
    String[] cats = {"soccer", "news", "weather"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID,
        Arrays.asList(cats), null, null);
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
    assertThat(tokens.get(1)).startsWith("http://server:8080/update/");
    assertThat(tokens.get(2)).startsWith("http://server:8080/update/");
  }

  @Test
  public void findPushEndpointsWithDeviceType() {
    String[] types = {"JavaFX Monitor"};
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null, null,
        Arrays.asList(types));
    assertThat(tokens).hasSize(1);
    assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
  }

  @Test
  public void findPushEndpointsWithoutDeviceType() {
    List<String> tokens = findAllDeviceTokenForVariantIDByCriteria(simplePushVariantID, null, null,
        null);
    assertThat(tokens).hasSize(3);
    assertThat(tokens.get(0)).startsWith("http://server:8080/update/");
  }

  @Test
  public void shouldValidateDeviceId() {
    // given
    final Installation installation = new Installation();
    installation.setDeviceToken("invalid");

    final iOSVariant variant = new iOSVariant();
    variant.setName("iOS Variant Name");
    variant.setPassphrase("12");
    variant.setProduction(false);
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
      final Set<ConstraintViolation<?>> constraintViolations = violationException
          .getConstraintViolations();
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
    variant.setName("iOS Variant Name");
    variant.setPassphrase("12");
    variant.setProduction(false);
    variant.setCertificate("12".getBytes());

    // when
    deviceTokenTest(installation, variant);
  }

  @Test
  public void shouldSaveWhenValidateDeviceIdAndroid() {
    // given
    final Installation installation = new Installation();
    installation.setDeviceToken(
        "APA91bHpbMXepp4odlb20vYOv0gQyNIyFu2X3OXR3TjqR8qecgWivima_UiLPFgUBs_10Nys2TUwUy"
            + "WlixrIta35NXW-5Z85OdXcbb_3s3p0qaa_a7NpFlaX9GpidK_BdQNMsx2gX8BrE4Uw7s22nPCcEn1U1_mo-"
            + "T6hcF5unYt965PDwRTRss8");

    final AndroidVariant variant = new AndroidVariant();
    variant.setName("Android Variant Name");
    variant.setGoogleKey("12");
    variant.setProjectNumber("12");

    // when
    deviceTokenTest(installation, variant);
  }

  @Test
  public void shouldSaveWhenValidateDeviceIdFromAndroidEmulator() {
    // given
    final Installation installation = new Installation();
    installation.setDeviceToken("eHlfnI0__dI:APA91bEhtHefML2lr_sBQ-bdXIyEn5owzkZg_p_y7SRyNKRMZ3Xu" +
        "zZhBpTOYIh46tqRYQIc-7RTADk4nM5H-ONgPDWHodQDS24O5GuKP8EZ" +
        "EKwNh4Zxdv1wkZJh7cU2PoLz9gn4Nxqz-");

    final AndroidVariant variant = new AndroidVariant();
    variant.setName("Android Variant Name");
    variant.setGoogleKey("12");
    variant.setProjectNumber("12");

    // when
    deviceTokenTest(installation, variant);
  }

  @Test
  public void shouldSaveWhenValidateDeviceIdFromFirebase() {
    // given
    final Installation installation = new Installation();
    installation
        .setDeviceToken("ckK-6WYMBXQ:APA91bEvCK_37qM89h5nKNOQTRz0rIjAMqP01hOi7QANlDDRpsDrB7w" +
            "382l_NB_6mNr_4l3Zx96IuL-dq9O4VdQnx8AM1-hquE2t4VkprbDrJ" +
            "2784ndbtAnt3FNg7J5aQcPPPO5g19An");

    final AndroidVariant variant = new AndroidVariant();
    variant.setName("Android Variant Name");
    variant.setGoogleKey("12");
    variant.setProjectNumber("12");

    // when
    deviceTokenTest(installation, variant);
  }

  @Test
  public void shouldSaveWhitAndroidInstanceIDtoken() {
    // given
    final Installation installation = new Installation();
    installation.setDeviceToken("cKNPkyd7HBU:APA91bH-GxYBT08qeltVLhFcPxvoT4MN1uRfhR-Q" +
        "Zns-0KLcZ159tQ14YXPOe4JjemCsGHLQNAqmFZrDNV-wcDqanGXWjHNG9ftNyBEj" +
        "7RqUOtpP1BEjTxWE4Hk7i1vKT3pyMV_7F8xF");

    final AndroidVariant variant = new AndroidVariant();
    variant.setName("Android Variant Name");
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
    final Set<Category> categoriesOne = new HashSet<>();
    final Category category = entityManager
        .createQuery("from Category where name = :name", Category.class)
        .setParameter("name", "soccer").getSingleResult();
    categoriesOne.add(category);
    android1.setCategories(categoriesOne);
    final String id = android1.getId();

    final AndroidVariant variant = new AndroidVariant();
    variant.setName("Android Variant Name");
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
    final PageResult<Installation, Count> pageResult = installationDao
        .findInstallationsByVariantForDeveloper(androidVariantID, developer, 0, 1, null);

    //then
    assertThat(pageResult).isNotNull();
    assertThat(pageResult.getResultList()).isNotEmpty().hasSize(1);
    assertThat(pageResult.getAggregate().getCount()).isEqualTo(6);
  }

  @Test
  public void shouldSelectInstallationsByVariant() {
    //when
    final PageResult<Installation, Count> pageResult = installationDao
        .findInstallationsByVariant(androidVariantID, 0, 1, null);

    //then
    assertThat(pageResult).isNotNull();
    assertThat(pageResult.getResultList()).isNotEmpty().hasSize(1);
    assertThat(pageResult.getAggregate().getCount()).isEqualTo(6);
  }

  @Test
  public void shouldSelectInstallationsByDeviceTokenSearch() {
    //when
    final PageResult<Installation, Count> pageResult = installationDao
        .findInstallationsByVariant(androidVariantID, 0, Integer.MAX_VALUE, "67890167890");
    //then
    assertThat(pageResult.getResultList()).isNotEmpty().hasSize(1);
  }

  @Test
  public void shouldSelectInstallationsByDeviceTypeSearch() {
    //when
    final PageResult<Installation, Count> pageResult = installationDao
        .findInstallationsByVariant(androidVariantID, 0, Integer.MAX_VALUE, "Tablet");
    //then
    assertThat(pageResult.getResultList()).isNotEmpty().hasSize(3);
  }

  @Test
  public void shouldSelectInstallationsByAliasSearch() {
    //when
    final PageResult<Installation, Count> pageResult = installationDao
        .findInstallationsByVariant(androidVariantID, 0, Integer.MAX_VALUE, "baz@");
    //then
    assertThat(pageResult.getResultList()).isNotEmpty().hasSize(1);
  }

  @Test(expected = PersistenceException.class)
  public void testTooLongDeviceToken() {
    AndroidVariant variant = new AndroidVariant();
    variant.setName("Android Name");
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
    variant.setName("Android Name");
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

  private List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID,
      List<String> categories, List<String> aliases, List<String> deviceTypes) {
    return findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes,
        false);
  }

  private List<String> findAllOldGCMDeviceTokenForVariantIDByCriteria(String variantID,
      List<String> categories, List<String> aliases, List<String> deviceTypes) {
    return findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes,
        true);
  }

  private List<String> findAllDeviceTokenForVariantIDByCriteria(String variantID,
      List<String> categories, List<String> aliases, List<String> deviceTypes, boolean oldGCM) {
    try {
      ResultsStream<String> tokenStream = installationDao
          .findAllDeviceTokenForVariantIDByCriteria(variantID, categories, aliases, deviceTypes,
              Integer.MAX_VALUE, null, oldGCM).executeQuery();
      List<String> list = new ArrayList<>();
      while (tokenStream.next()) {
        list.add(tokenStream.get());
      }
      return list;
    } catch (ResultStreamException e) {
      throw new IllegalStateException(e);
    }
  }

}
