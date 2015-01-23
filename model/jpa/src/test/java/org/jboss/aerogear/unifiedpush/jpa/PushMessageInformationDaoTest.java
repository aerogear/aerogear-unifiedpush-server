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
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.utils.DateUtils;
import org.jboss.aerogear.unifiedpush.utils.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class PushMessageInformationDaoTest {

    private EntityManager entityManager;
    private JPAPushMessageInformationDao pushMessageInformationDao;
    private PushMessageInformation pushMessageInformation;
    private String pushMessageInformationID;

    @Before
    public void setUp() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("UnifiedPush");
        entityManager = emf.createEntityManager();

        // start the shindig
        entityManager.getTransaction().begin();

        pushMessageInformationDao = new JPAPushMessageInformationDao();
        pushMessageInformationDao.setEntityManager(entityManager);

        // some raw data:
        pushMessageInformation = new PushMessageInformation();

        // old date:
        final Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(1980, Calendar.FEBRUARY, 1);
        pushMessageInformation.setSubmitDate(calendar.getTime());

        pushMessageInformation.setPushApplicationId("231231231");
        pushMessageInformationID = pushMessageInformation.getId();

        VariantMetricInformation variantOne = new VariantMetricInformation();
        variantOne.setDeliveryStatus(Boolean.FALSE);
        variantOne.setReceivers(200);
        variantOne.setVariantID("213");
        pushMessageInformation.addVariantInformations(variantOne);

        pushMessageInformationDao.create(pushMessageInformation);

        flushAndClear();

    }

    private void flushAndClear() {
        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();
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
    public void createPushMessageInformation() {

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        assertThat(pushMessageInformation).isNotNull();
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }

    @Test
    public void addJsonToPushMessageInformation() {
        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);
        pushMessageInformation.setRawJsonMessage("{\"alert\" : \"hello\"}");
        pushMessageInformationDao.update(pushMessageInformation);

        flushAndClear();

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        assertThat(pushMessageInformation.getRawJsonMessage()).isEqualTo("{\"alert\" : \"hello\"}");
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }

    @Test
    public void addClientIdentifierToPushMessageInformation() {
        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);
        pushMessageInformation.setClientIdentifier("Java Sender Client");
        pushMessageInformationDao.update(pushMessageInformation);

        flushAndClear();

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        assertThat(pushMessageInformation.getClientIdentifier()).isEqualTo("Java Sender Client");
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }

    @Test
    public void addVariantInfoToPushMessageInformation() {
        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        VariantMetricInformation variantOne = new VariantMetricInformation();
        variantOne.setDeliveryStatus(Boolean.FALSE);
        variantOne.setReceivers(200);
        variantOne.setVariantID("231543432432");
        pushMessageInformation.getVariantInformations().add(variantOne);

        VariantMetricInformation variantTwo = new VariantMetricInformation();
        variantTwo.setDeliveryStatus(Boolean.TRUE);
        variantTwo.setReceivers(2000);
        variantTwo.setVariantID("2315403432433");
        pushMessageInformation.getVariantInformations().add(variantTwo);

        pushMessageInformationDao.update(pushMessageInformation);

        flushAndClear();

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);


        assertThat(pushMessageInformation.getVariantInformations()).extracting("receivers", "deliveryStatus")
                .contains(
                        tuple(2000L, Boolean.TRUE),
                        tuple(200L, Boolean.FALSE)
                );

        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }

    @Test
    public void findByPushApplicationID() {
        int page = 0;
        int pageSize = 20;
        PageResult<PushMessageInformation> messageInformations = pushMessageInformationDao.findAllForPushApplication("231231231", Boolean.TRUE, page, pageSize);
        assertThat(messageInformations.getResultList()).isNotEmpty();
        assertThat(messageInformations.getResultList()).hasSize(1);
    }

    @Test
    public void countMessagesPerApplicationID() {

        final String loginName = "admin";

        final PushApplication pushApp = new PushApplication();
        pushApp.setName("app");
        pushApp.setPushApplicationID("231231231");
        pushApp.setDeveloper(loginName);
        entityManager.persist(pushApp);

        final PushApplication pushApp1 = new PushApplication();
        pushApp1.setName("app1");
        pushApp1.setPushApplicationID("231231232");
        pushApp1.setDeveloper(loginName);
        entityManager.persist(pushApp1);

        long number = pushMessageInformationDao.getNumberOfPushMessagesForApplications(loginName);
        assertThat(number).isEqualTo(1);

        for (int i = 0; i < 100; i++) {

            PushMessageInformation pmi = new PushMessageInformation();
            pmi.setPushApplicationId(pushApp.getPushApplicationID());

            pushMessageInformationDao.create(pmi);
        }

        number = pushMessageInformationDao.getNumberOfPushMessagesForApplications(loginName);
        assertThat(number).isEqualTo(101);

        // a few more for different PushApplication...
        for (int i = 0; i < 100; i++) {

            PushMessageInformation pmi = new PushMessageInformation();
            pmi.setPushApplicationId(pushApp1.getPushApplicationID());

            pushMessageInformationDao.create(pmi);
        }

        number = pushMessageInformationDao.getNumberOfPushMessagesForApplications(loginName);
        assertThat(number).isEqualTo(201);
    }

    @Test
    public void findPushMessageInformationsPerVariant() {

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        VariantMetricInformation variantOne = new VariantMetricInformation();
        variantOne.setDeliveryStatus(Boolean.FALSE);
        variantOne.setReceivers(200);
        variantOne.setVariantID("231543432432");
        pushMessageInformation.getVariantInformations().add(variantOne);
        pushMessageInformationDao.update(pushMessageInformation);

        VariantMetricInformation variantThree = new VariantMetricInformation();
        variantThree.setDeliveryStatus(Boolean.FALSE);
        variantThree.setReceivers(200);
        variantThree.setVariantID("23154343243333");
        pushMessageInformation.getVariantInformations().add(variantThree);
        pushMessageInformationDao.update(pushMessageInformation);

        VariantMetricInformation variantFour = new VariantMetricInformation();
        variantFour.setDeliveryStatus(Boolean.FALSE);
        variantFour.setReceivers(200);
        variantFour.setVariantID("231543432434");
        pushMessageInformation.getVariantInformations().add(variantFour);
        pushMessageInformationDao.update(pushMessageInformation);

        PushMessageInformation pmi = new PushMessageInformation();
        pmi.setPushApplicationId("231231231");
        pushMessageInformationDao.create(pmi);
        VariantMetricInformation variantTwo = new VariantMetricInformation();
        variantTwo.setDeliveryStatus(Boolean.TRUE);
        variantTwo.setReceivers(2000);
        variantTwo.setVariantID("231543432432");
        pmi.getVariantInformations().add(variantTwo);
        pushMessageInformationDao.update(pmi);



        flushAndClear();

        assertThat(pushMessageInformationDao.findAllForVariant("231543432432", Boolean.TRUE, 0, 25).getResultList()).hasSize(2);
        assertThat(pushMessageInformationDao.findAllForVariant("23154343243333", Boolean.TRUE, 0, 25).getResultList()).hasSize(1);
    }

    @Test
    public void findMostBusyVariants() {
        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        final String loginName = "admin";

        final PushApplication pushAppInitial = new PushApplication();
        pushAppInitial.setName("app");
        pushAppInitial.setPushApplicationID("231231231");
        pushAppInitial.setDeveloper(loginName);
        entityManager.persist(pushAppInitial);

        VariantMetricInformation variantOne = new VariantMetricInformation();
        variantOne.setDeliveryStatus(Boolean.FALSE);
        variantOne.setReceivers(200);
        variantOne.setVariantID("231543432432");
        pushMessageInformation.getVariantInformations().add(variantOne);
        pushMessageInformationDao.update(pushMessageInformation);

        VariantMetricInformation variantThree = new VariantMetricInformation();
        variantThree.setDeliveryStatus(Boolean.FALSE);
        variantThree.setReceivers(300);
        variantThree.setVariantID("23154343243333");
        pushMessageInformation.getVariantInformations().add(variantThree);
        pushMessageInformationDao.update(pushMessageInformation);

        VariantMetricInformation variantFour = new VariantMetricInformation();
        variantFour.setDeliveryStatus(Boolean.FALSE);
        variantFour.setReceivers(1000);
        variantFour.setVariantID("231543432434");
        pushMessageInformation.getVariantInformations().add(variantFour);
        pushMessageInformationDao.update(pushMessageInformation);

        final PushApplication pushApp = new PushApplication();
        pushApp.setName("app");
        pushApp.setPushApplicationID("231231232");
        pushApp.setDeveloper(loginName);
        entityManager.persist(pushApp);

        PushMessageInformation pmi = new PushMessageInformation();
        pmi.setPushApplicationId("231231232");
        pushMessageInformationDao.create(pmi);
        VariantMetricInformation variantTwo = new VariantMetricInformation();
        variantTwo.setDeliveryStatus(Boolean.TRUE);
        variantTwo.setReceivers(2000);
        variantTwo.setVariantID("231543432432");
        pmi.getVariantInformations().add(variantTwo);
        pushMessageInformationDao.update(pmi);

        final PushApplication pushApp1 = new PushApplication();
        pushApp1.setName("app");
        pushApp1.setPushApplicationID("231231233");
        pushApp1.setDeveloper(loginName);
        entityManager.persist(pushApp1);

        PushMessageInformation pmi1 = new PushMessageInformation();
        pmi1.setPushApplicationId("231231233");
        pushMessageInformationDao.create(pmi);
        VariantMetricInformation variantSix= new VariantMetricInformation();
        variantSix.setDeliveryStatus(Boolean.TRUE);
        variantSix.setReceivers(2000);
        variantSix.setVariantID("231543432432");
        pmi1.getVariantInformations().add(variantSix);
        pushMessageInformationDao.update( pmi1);


        final AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setGoogleKey("123");
        androidVariant.setVariantID("231543432432");
        androidVariant.setDeveloper(loginName);
        entityManager.persist(androidVariant);

        final AndroidVariant androidVariant1 = new AndroidVariant();
        androidVariant1.setGoogleKey("123");
        androidVariant1.setVariantID("23154343243333");
        androidVariant1.setDeveloper(loginName);
        entityManager.persist(androidVariant1);

        final AndroidVariant androidVariant2 = new AndroidVariant();
        androidVariant2.setGoogleKey("123");
        androidVariant2.setVariantID("231543432434");
        androidVariant2.setDeveloper(loginName);
        entityManager.persist(androidVariant2);

        flushAndClear();

        List<PushMessageInformation> lastActivity = pushMessageInformationDao.findLastThreeActivity(loginName);
        assertThat(lastActivity).hasSize(3);

    }

    @Test
    public void findMostBusyVariantsForOnlyTwo() {
        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        final String loginName = "admin";

        final PushApplication pushAppInitial = new PushApplication();
        pushAppInitial.setName("app");
        pushAppInitial.setPushApplicationID("231231231");
        pushAppInitial.setDeveloper(loginName);
        entityManager.persist(pushAppInitial);

        VariantMetricInformation variantOne = new VariantMetricInformation();
        variantOne.setDeliveryStatus(Boolean.FALSE);
        variantOne.setReceivers(200);
        variantOne.setVariantID("231543432432");
        pushMessageInformation.getVariantInformations().add(variantOne);
        pushMessageInformationDao.update(pushMessageInformation);

        VariantMetricInformation variantThree = new VariantMetricInformation();
        variantThree.setDeliveryStatus(Boolean.FALSE);
        variantThree.setReceivers(300);
        variantThree.setVariantID("23154343243333");
        pushMessageInformation.getVariantInformations().add(variantThree);
        pushMessageInformationDao.update(pushMessageInformation);

        VariantMetricInformation variantFour = new VariantMetricInformation();
        variantFour.setDeliveryStatus(Boolean.FALSE);
        variantFour.setReceivers(1000);
        variantFour.setVariantID("231543432434");
        pushMessageInformation.getVariantInformations().add(variantFour);
        pushMessageInformationDao.update(pushMessageInformation);

        final PushApplication pushApp = new PushApplication();
        pushApp.setName("app");
        pushApp.setPushApplicationID("231231232");
        pushApp.setDeveloper(loginName);
        entityManager.persist(pushApp);

        PushMessageInformation pmi = new PushMessageInformation();
        pmi.setPushApplicationId("231231232");
        pushMessageInformationDao.create(pmi);
        VariantMetricInformation variantTwo = new VariantMetricInformation();
        variantTwo.setDeliveryStatus(Boolean.TRUE);
        variantTwo.setReceivers(2000);
        variantTwo.setVariantID("231543432432");
        pmi.getVariantInformations().add(variantTwo);
        pushMessageInformationDao.update(pmi);

        final AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setGoogleKey("123");
        androidVariant.setVariantID("231543432432");
        androidVariant.setDeveloper(loginName);
        entityManager.persist(androidVariant);

        final AndroidVariant androidVariant1 = new AndroidVariant();
        androidVariant1.setGoogleKey("123");
        androidVariant1.setVariantID("23154343243333");
        androidVariant1.setDeveloper(loginName);
        entityManager.persist(androidVariant1);

        final AndroidVariant androidVariant2 = new AndroidVariant();
        androidVariant2.setGoogleKey("123");
        androidVariant2.setVariantID("231543432434");
        androidVariant2.setDeveloper(loginName);
        entityManager.persist(androidVariant2);

        flushAndClear();

        List<PushMessageInformation> lastActivity = pushMessageInformationDao.findLastThreeActivity(loginName);
        assertThat(lastActivity).hasSize(2);

    }

    @Test
    public void findVariantIDsWithWarnings() {

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        final String loginName = "admin";

        VariantMetricInformation variantOne = new VariantMetricInformation();
        variantOne.setDeliveryStatus(Boolean.FALSE);
        variantOne.setReceivers(200);
        variantOne.setVariantID("231543432432");
        pushMessageInformation.getVariantInformations().add(variantOne);
        pushMessageInformationDao.update(pushMessageInformation);

        VariantMetricInformation variantThree = new VariantMetricInformation();
        variantThree.setDeliveryStatus(Boolean.FALSE);
        variantThree.setReceivers(200);
        variantThree.setVariantID("23154343243333");
        pushMessageInformation.getVariantInformations().add(variantThree);
        pushMessageInformationDao.update(pushMessageInformation);

        VariantMetricInformation variantFour = new VariantMetricInformation();
        variantFour.setDeliveryStatus(Boolean.TRUE);
        variantFour.setReceivers(200);
        variantFour.setVariantID("231543432434");
        pushMessageInformation.getVariantInformations().add(variantFour);
        pushMessageInformationDao.update(pushMessageInformation);

        final AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setGoogleKey("123");
        androidVariant.setVariantID("231543432432");
        androidVariant.setDeveloper("someone");
        entityManager.persist(androidVariant);

        final AndroidVariant androidVariant1 = new AndroidVariant();
        androidVariant1.setGoogleKey("123");
        androidVariant1.setVariantID("23154343243333");
        androidVariant1.setDeveloper(loginName);
        entityManager.persist(androidVariant1);

        final AndroidVariant androidVariant2 = new AndroidVariant();
        androidVariant2.setGoogleKey("123");
        androidVariant2.setVariantID("231543432434");
        androidVariant2.setDeveloper("other");
        entityManager.persist(androidVariant2);
        flushAndClear();

        // all warnings:
        final List<String> variantIDsWithWarnings = pushMessageInformationDao.findVariantIDsWithWarnings();

        assertThat(variantIDsWithWarnings).hasSize(2);
        assertThat(variantIDsWithWarnings).contains("231543432432", "23154343243333");
    }

    @Test
    public void ascendingDateOrdering() {

        PushMessageInformation pmi = new PushMessageInformation();
        pmi.setPushApplicationId("231231231");
        pushMessageInformationDao.create(pmi);
        VariantMetricInformation variantTwo = new VariantMetricInformation();
        variantTwo.setDeliveryStatus(Boolean.TRUE);
        variantTwo.setReceivers(2000);
        variantTwo.setVariantID("231543432432");
        pmi.getVariantInformations().add(variantTwo);
        pushMessageInformationDao.update(pmi);


        PageResult<PushMessageInformation> messageInformations =
                pushMessageInformationDao.findAllForPushApplication("231231231", Boolean.TRUE, 0, 25);
        final List<PushMessageInformation> list = messageInformations.getResultList();
        assertThat(list).hasSize(2);

        assertThat(list.get(0).getSubmitDate()).isBefore(list.get(1).getSubmitDate());
    }

    @Test
    public void descendingDateOrdering() {

        PushMessageInformation pmi = new PushMessageInformation();
        pmi.setPushApplicationId("231231231");
        pushMessageInformationDao.create(pmi);
        VariantMetricInformation variantTwo = new VariantMetricInformation();
        variantTwo.setDeliveryStatus(Boolean.TRUE);
        variantTwo.setReceivers(2000);
        variantTwo.setVariantID("231543432432");
        pmi.getVariantInformations().add(variantTwo);
        pushMessageInformationDao.update(pmi);


        PageResult<PushMessageInformation> messageInformations =
                pushMessageInformationDao.findAllForPushApplication("231231231", Boolean.FALSE, 0, 25);
        final List<PushMessageInformation> list = messageInformations.getResultList();
        assertThat(list).hasSize(2);

        assertThat(list.get(0).getSubmitDate()).isAfter(list.get(1).getSubmitDate());
    }

    @Test
    public void testLongRawJsonPayload() {
        PushMessageInformation largePushMessageInformation = new PushMessageInformation();
        largePushMessageInformation.setPushApplicationId("231231231");
        largePushMessageInformation.setRawJsonMessage(TestUtils.longString(4500));
        pushMessageInformationDao.create(largePushMessageInformation);
    }

    @Test(expected = PersistenceException.class)
    public void testTooLongRawJsonPayload() {
        PushMessageInformation largePushMessageInformation = new PushMessageInformation();
        largePushMessageInformation.setPushApplicationId("231231231");
        largePushMessageInformation.setRawJsonMessage(TestUtils.longString(4501));
        pushMessageInformationDao.create(largePushMessageInformation);
        flushAndClear();
    }

    @Test
    public void deleteOldPushMessageInformations() {

        List<PushMessageInformation> messageInformations = pushMessageInformationDao.findAllForPushApplication("231231231", Boolean.TRUE);
        assertThat(messageInformations).hasSize(1);

        pushMessageInformationDao.deletePushInformationOlderThan(DateUtils.calculatePastDate(0));

        flushAndClear();

        messageInformations = pushMessageInformationDao.findAllForPushApplication("231231231", Boolean.TRUE);
        assertThat(messageInformations).hasSize(0);
    }
}
