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

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.jpa.dao.impl.JPAPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.utils.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.Arrays;
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
        pushMessageInformation.setPushApplicationId("231231231");
        pushMessageInformationID = pushMessageInformation.getId();


        VariantMetricInformation variantOne = new VariantMetricInformation();
        variantOne.setDeliveryStatus(Boolean.FALSE);
        variantOne.setReceivers(200);
        variantOne.setVariantID("213");
        pushMessageInformation.getVariantInformations().add(variantOne);


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
        entityManager.getTransaction().commit();

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

        List<PushMessageInformation> messageInformations = pushMessageInformationDao.findAllForPushApplication("231231231");
        assertThat(messageInformations).isNotEmpty();
        assertThat(messageInformations).hasSize(1);
    }

    @Test
    public void countMessagesPerApplicationID() {

        final String onePushApplicationID = "231231231";
        final String secondPushApplicationID = "231231232";

        long number = pushMessageInformationDao.getNumberOfPushMessagesForApplications(Arrays.asList(onePushApplicationID, secondPushApplicationID));
        assertThat(number).isEqualTo(1);

        for (int i = 0; i < 100; i++) {

            PushMessageInformation pmi = new PushMessageInformation();
            pmi.setPushApplicationId(onePushApplicationID);

            pushMessageInformationDao.create(pmi);
        }

        number = pushMessageInformationDao.getNumberOfPushMessagesForApplications(Arrays.asList(onePushApplicationID, secondPushApplicationID));
        assertThat(number).isEqualTo(101);

        // a few more for different PushApplication...
        for (int i = 0; i < 100; i++) {

            PushMessageInformation pmi = new PushMessageInformation();
            pmi.setPushApplicationId(secondPushApplicationID);

            pushMessageInformationDao.create(pmi);
        }

        number = pushMessageInformationDao.getNumberOfPushMessagesForApplications(Arrays.asList(onePushApplicationID, secondPushApplicationID));
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

        assertThat(pushMessageInformationDao.findAllForVariant("231543432432")).hasSize(2);
        assertThat(pushMessageInformationDao.findAllForVariant("23154343243333")).hasSize(1);
    }


    @Test
    public void deleteOldPushMessageInformations() throws InterruptedException {

        // let's wait a bit...
        Thread.sleep(1000);




        pushMessageInformationDao.deletePushInformationOlderThan(DateUtils.calculatePastDate(0));


    }

}
