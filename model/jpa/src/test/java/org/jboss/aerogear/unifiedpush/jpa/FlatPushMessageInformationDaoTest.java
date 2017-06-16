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
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantErrorStatus;
import org.jboss.aerogear.unifiedpush.dao.FlatPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.dto.MessageMetrics;
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
//import static org.assertj.core.api.Assertions.tuple;

@RunWith(Arquillian.class)
public class FlatPushMessageInformationDaoTest {

    @Inject
    private EntityManager entityManager;

    @Inject
    private FlatPushMessageInformationDao pushMessageInformationDao;

    @Inject
    private VariantDao variantDao;

//    @Inject
//    private JPAVariantMetricInformationDao variantMetricInformationDao;
    private String pushMessageInformationID = "1";

    @Deployment
    public static JavaArchive createDeployment() {
        return DaoDeployment.createDeployment();
    }

    @Rule
    public EmbeddedDbTesterRule testDb = new EmbeddedDbTesterRule("FlatPushMessageInformation.xml");


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

        FlatPushMessageInformation pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        assertThat(pushMessageInformation).isNotNull();
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }

    @Test
    public void addJsonToPushMessageInformation() {
        FlatPushMessageInformation pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);
        pushMessageInformation.setRawJsonMessage("{\"alert\" : \"hello\"}");
        pushMessageInformationDao.update(pushMessageInformation);

        flushAndClear();

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        assertThat(pushMessageInformation.getRawJsonMessage()).isEqualTo("{\"alert\" : \"hello\"}");
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }

    @Test
    public void addClientIdentifierToPushMessageInformation() {
        FlatPushMessageInformation pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);
        pushMessageInformation.setClientIdentifier("Java Sender Client");
        pushMessageInformationDao.update(pushMessageInformation);

        flushAndClear();

        pushMessageInformation = pushMessageInformationDao.find(pushMessageInformationID);

        assertThat(pushMessageInformation.getClientIdentifier()).isEqualTo("Java Sender Client");
        assertThat(pushMessageInformation.getSubmitDate()).isNotNull();
    }


    @Test
    public void findByPushApplicationID() {
        int page = 0;
        int pageSize = 20;
        PageResult<FlatPushMessageInformation, MessageMetrics> messageInformations = pushMessageInformationDao.findAllForPushApplication("231231231", null, Boolean.TRUE, page, pageSize);
        assertThat(messageInformations.getResultList()).isNotEmpty();
        assertThat(messageInformations.getResultList()).hasSize(2);
    }

    @Test
    public void countMessagesPerLoginName() {

        final String loginName = "admin";

        long number = pushMessageInformationDao.getNumberOfPushMessagesForLoginName(loginName);
        assertThat(number).isEqualTo(3);

        for (int i = 0; i < 1000; i++) {

            FlatPushMessageInformation pmi = new FlatPushMessageInformation();
            pmi.setPushApplicationId("231231231");

            pushMessageInformationDao.create(pmi);
        }

        number = pushMessageInformationDao.getNumberOfPushMessagesForLoginName(loginName);
        assertThat(number).isEqualTo(1003);

        // a few more for different PushApplication...
        for (int i = 0; i < 1000; i++) {

            FlatPushMessageInformation pmi = new FlatPushMessageInformation();
            pmi.setPushApplicationId("231231232");

            pushMessageInformationDao.create(pmi);
        }

        number = pushMessageInformationDao.getNumberOfPushMessagesForLoginName(loginName);
        assertThat(number).isEqualTo(2003);

        // check for all
        assertThat(pushMessageInformationDao.getNumberOfPushMessagesForApplications()).isEqualTo(2003);
    }

    @Test
    public void countMessagesPerApplication() {
        assertThat(pushMessageInformationDao.getNumberOfPushMessagesForPushApplication("231231231")).isEqualTo(2);
        assertThat(pushMessageInformationDao.getNumberOfPushMessagesForPushApplication("231231232")).isEqualTo(1);
    }

    @Test
    public void findMostBusyVariants() {
        List<FlatPushMessageInformation> lastActivity = pushMessageInformationDao.findLatestActivity("admin", 3);
        assertThat(lastActivity).hasSize(3);
    }

    @Test
    public void findAllBusyVariants() {
        List<FlatPushMessageInformation> lastActivity = pushMessageInformationDao.findLatestActivity("admin", 5);
        assertThat(lastActivity).hasSize(3); // we just have three... - but asked for five
    }

    @Test
    public void findMostBusyVariantsForOnlyTwo() {
        final FlatPushMessageInformation pushMessageInformation = pushMessageInformationDao.find("3");
        entityManager.remove(pushMessageInformation);
        List<FlatPushMessageInformation> lastActivity = pushMessageInformationDao.findLatestActivity("admin", 3);
        assertThat(lastActivity).hasSize(2);

        lastActivity = pushMessageInformationDao.findLatestActivity(3);
        assertThat(lastActivity).hasSize(2);
    }

    @Test
    public void findVariantIDsWithWarnings() {
        // all warnings:
        final List<String> variantIDsWithWarnings = pushMessageInformationDao.findVariantIDsWithWarnings();

        assertThat(variantIDsWithWarnings).hasSize(4);
        assertThat(variantIDsWithWarnings).contains("1", "2", "3", "4");
    }

    @Test
    public void findVariantIDsWithWarningsForDude() {
        // all warnings:
        final List<String> variantIDsWithWarnings = pushMessageInformationDao.findVariantIDsWithWarnings("dude");

        assertThat(variantIDsWithWarnings).hasSize(1);
        assertThat(variantIDsWithWarnings).containsOnly("4");
    }

    @Test
    public void findVariantIDsWithWarningsForKalle() {
        // all warnings:
        final List<String> variantIDsWithWarnings = pushMessageInformationDao.findVariantIDsWithWarnings("kalle");

        assertThat(variantIDsWithWarnings).isNotNull();
        assertThat(variantIDsWithWarnings).isEmpty();
    }

    @Test
    public void addTwoErrors() {
        FlatPushMessageInformation fmpi = pushMessageInformationDao.find("1");
        Variant variant = variantDao.find("1");
        VariantErrorStatus ves = new VariantErrorStatus(fmpi, variant, "error");

        ves.setVariant(variant);
        ves.setPushMessageInformation(fmpi);

        fmpi.getErrors().add(ves);
    }

    @Test
    public void ascendingDateOrdering() {

        PageResult<FlatPushMessageInformation, MessageMetrics> messageInformations =
                pushMessageInformationDao.findAllForPushApplication("231231231", null, Boolean.TRUE, 0, 25);
        final List<FlatPushMessageInformation> list = messageInformations.getResultList();
        assertThat(list).hasSize(2);

        assertThat(list.get(0).getSubmitDate()).isBefore(list.get(1).getSubmitDate());
    }

    @Test
    public void descendingDateOrdering() {
        PageResult<FlatPushMessageInformation, MessageMetrics> messageInformations =
                pushMessageInformationDao.findAllForPushApplication("231231231", null, Boolean.FALSE, 0, 25);
        final List<FlatPushMessageInformation> list = messageInformations.getResultList();
        assertThat(list).hasSize(2);

        assertThat(list.get(0).getSubmitDate()).isAfter(list.get(1).getSubmitDate());
    }

    @Test
    public void testSearchString() {
        PageResult<FlatPushMessageInformation, MessageMetrics> messageInformations =
                pushMessageInformationDao.findAllForPushApplication("231231231", "foo", Boolean.TRUE, 0, 25);
        final List<FlatPushMessageInformation> list = messageInformations.getResultList();
        assertThat(list).hasSize(1);
    }

    @Test
    public void testLongRawJsonPayload() {
        FlatPushMessageInformation largePushMessageInformation = new FlatPushMessageInformation();
        largePushMessageInformation.setPushApplicationId("231231231");
        largePushMessageInformation.setRawJsonMessage(TestUtils.longString(4500));
        pushMessageInformationDao.create(largePushMessageInformation);
    }

    @Test(expected = PersistenceException.class)
    public void testTooLongRawJsonPayload() {
        FlatPushMessageInformation largePushMessageInformation = new FlatPushMessageInformation();
        largePushMessageInformation.setPushApplicationId("231231231");
        largePushMessageInformation.setRawJsonMessage(TestUtils.longString(4501));
        pushMessageInformationDao.create(largePushMessageInformation);
        flushAndClear();
    }

    @Test
    public void deleteOldPushMessageInformations() {

        List<FlatPushMessageInformation> messageInformations = pushMessageInformationDao.findAllForPushApplication("231231231", Boolean.TRUE);
        assertThat(messageInformations).hasSize(2);

        pushMessageInformationDao.deletePushInformationOlderThan(DateUtils.calculatePastDate(0));

        flushAndClear();

        messageInformations = pushMessageInformationDao.findAllForPushApplication("231231231", Boolean.TRUE);
        assertThat(messageInformations).hasSize(0);
    }
}
