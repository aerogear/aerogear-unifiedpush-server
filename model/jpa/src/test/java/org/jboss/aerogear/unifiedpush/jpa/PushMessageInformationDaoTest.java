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
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.utils.DaoDeployment;
import org.jboss.aerogear.unifiedpush.utils.DateUtils;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(Arquillian.class)
public class PushMessageInformationDaoTest {

    @Inject
    private EntityManager entityManager;
    @Inject
    private PushMessageInformationDao pushMessageInformationDao;
    private String pushMessageInformationID = "1";

    @Deployment
    public static JavaArchive createDeployment() {
        return DaoDeployment.createDeployment();
    }

    @Rule
    public EmbeddedDbTesterRule testDb = new EmbeddedDbTesterRule("MessageInformation.xml");

    @Before
    public void setUp() {
        // start the shindig
        entityManager.getTransaction().begin();
    }

    private void flushAndClear() {
        // flush to be sure that it's in the database
        entityManager.flush();
        // clear the cache otherwise finding the entity will not perform a select but get the entity from cache
        entityManager.clear();
    }

    @After
    public void tearDown() {
        entityManager.getTransaction().rollback();
    }

    @Test
    public void createPushMessageInformation() {

        PushMessageInformation pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        assertThat(pushMessageInformation).isNotNull();
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }

    @Test
    public void addJsonToPushMessageInformation() {
        PushMessageInformation pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);
        pushMessageInformation.setRawJsonMessage("{\"alert\" : \"hello\"}");
        pushMessageInformationDao.update(pushMessageInformation);

        flushAndClear();

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        assertThat(pushMessageInformation.getRawJsonMessage()).isEqualTo("{\"alert\" : \"hello\"}");
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }

    @Test
    public void addClientIdentifierToPushMessageInformation() {
        PushMessageInformation pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);
        pushMessageInformation.setClientIdentifier("Java Sender Client");
        pushMessageInformationDao.update(pushMessageInformation);

        flushAndClear();

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        assertThat(pushMessageInformation.getClientIdentifier()).isEqualTo("Java Sender Client");
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }

    @Test
    public void addVariantInfoToPushMessageInformation() {
        PushMessageInformation pushMessageInformation = pushMessageInformationDao.find("2");

        assertThat(pushMessageInformation.getVariantInformations()).extracting("receivers", "deliveryStatus")
                .contains(
                        tuple(1000L, Boolean.FALSE),
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
        assertThat(messageInformations.getResultList()).hasSize(2);
    }

    @Test
    public void countMessagesPerApplicationID() {

        final String loginName = "admin";

        long number = pushMessageInformationDao.getNumberOfPushMessagesForApplications(loginName);
        assertThat(number).isEqualTo(3);

        for (int i = 0; i < 100; i++) {

            PushMessageInformation pmi = new PushMessageInformation();
            pmi.setPushApplicationId("231231231");

            pushMessageInformationDao.create(pmi);
        }

        number = pushMessageInformationDao.getNumberOfPushMessagesForApplications(loginName);
        assertThat(number).isEqualTo(103);

        // a few more for different PushApplication...
        for (int i = 0; i < 100; i++) {

            PushMessageInformation pmi = new PushMessageInformation();
            pmi.setPushApplicationId("231231232");

            pushMessageInformationDao.create(pmi);
        }

        number = pushMessageInformationDao.getNumberOfPushMessagesForApplications(loginName);
        assertThat(number).isEqualTo(203);
    }

    @Test
    public void findPushMessageInformationsPerVariant() {
        assertThat(pushMessageInformationDao.findAllForVariant("231543432432", Boolean.TRUE, 0, 25).getResultList()).hasSize(2);
        assertThat(pushMessageInformationDao.findAllForVariant("23154343243333", Boolean.TRUE, 0, 25).getResultList()).hasSize(1);
    }

    @Test
    public void findMostBusyVariants() {
        List<PushMessageInformation> lastActivity = pushMessageInformationDao.findLastThreeActivity("admin");
        assertThat(lastActivity).hasSize(3);
    }

    @Test
    public void findMostBusyVariantsForOnlyTwo() {
        final PushMessageInformation pushMessageInformation = pushMessageInformationDao.find("3");
        entityManager.remove(pushMessageInformation);
        List<PushMessageInformation> lastActivity = pushMessageInformationDao.findLastThreeActivity("admin");
        assertThat(lastActivity).hasSize(2);

    }

    @Test
    public void findVariantIDsWithWarnings() {
        // all warnings:
        final List<String> variantIDsWithWarnings = pushMessageInformationDao.findVariantIDsWithWarnings();

        assertThat(variantIDsWithWarnings).hasSize(3);
        assertThat(variantIDsWithWarnings).contains("231543432432", "23154343243333", "231543432434");
    }

    @Test
    public void ascendingDateOrdering() {

        PageResult<PushMessageInformation> messageInformations =
                pushMessageInformationDao.findAllForPushApplication("231231231", Boolean.TRUE, 0, 25);
        final List<PushMessageInformation> list = messageInformations.getResultList();
        assertThat(list).hasSize(2);

        assertThat(list.get(0).getSubmitDate()).isBefore(list.get(1).getSubmitDate());
    }

    @Test
    public void descendingDateOrdering() {
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
        assertThat(messageInformations).hasSize(2);

        pushMessageInformationDao.deletePushInformationOlderThan(DateUtils.calculatePastDate(0));

        flushAndClear();

        messageInformations = pushMessageInformationDao.findAllForPushApplication("231231231", Boolean.TRUE);
        assertThat(messageInformations).hasSize(0);
    }
}
