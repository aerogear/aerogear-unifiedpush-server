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

package org.jboss.aerogear.unifiedpush.service;


import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.VariantDao;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.junit.Test;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class PushMessageMetricServiceTest extends AbstractBaseServiceTest {

    @Inject
    private PushMessageMetricsService pushMessageMetricsService;

    @Inject
    private VariantDao variantDao;

    private FlatPushMessageInformation pushMessageInformation;

    @Override
    protected void specificSetup() {
        pushMessageInformation =
                pushMessageMetricsService.storeNewRequestFrom(
                        "123",
                        "hello",
                        "127.0.01",
                        "testcase"
                );

         AndroidVariant v = new AndroidVariant();
         v.setVariantID("321");
         v.setId("321");
         v.setSecret("secret");
         v.setName("Android test variant");
         v.setType(VariantType.ANDROID);
         v.setGoogleKey("12345678");
         variantDao.create(v);

    }

    @Test
    public void updateAnalyticsTest() {
        pushMessageMetricsService.updateAnalytics(pushMessageInformation.getId(),"321");
        FlatPushMessageInformation updatedPushInformation = pushMessageMetricsService.getPushMessageInformation(pushMessageInformation.getId());
        assertThat(updatedPushInformation.getAppOpenCounter()).isEqualTo(1);

        pushMessageMetricsService.updateAnalytics(pushMessageInformation.getId(),"321");
        FlatPushMessageInformation updatedPushInformation1 = pushMessageMetricsService.getPushMessageInformation(pushMessageInformation.getId());
        assertThat(updatedPushInformation1.getAppOpenCounter()).isEqualTo(2);
    }

    @Test
    public void errorCounter() {
//        pushMessageMetricsService.appendError(pushMessageInformation, "321", "Big failure");
        pushMessageMetricsService.appendError(pushMessageInformation, variantDao.findByVariantID("321"), "Really big failure");
        pushMessageMetricsService.updatePushMessageInformation(pushMessageInformation);

        FlatPushMessageInformation updatedPushInformation = pushMessageMetricsService.getPushMessageInformation(pushMessageInformation.getId());
        assertThat(updatedPushInformation.getErrors().size()).isEqualTo(1);
        assertThat(updatedPushInformation.getErrors())
                .extracting("pushMessageVariantId", "variantID", "errorReason")
                .contains(
//                        tuple(updatedPushInformation.getId() + ":321", "321", "Big failure" ),
                        tuple(updatedPushInformation.getId() + ":321", "321", "Really big failure" )
                );
    }

}
