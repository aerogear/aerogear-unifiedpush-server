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


import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.dao.VariantMetricInformationDao;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.junit.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

public class PushMessageMetricServiceTest extends AbstractBaseServiceTest {

    @Inject
    private PushMessageMetricsService pushMessageMetricsService;

    @Inject
    private VariantMetricInformationDao variantMetricInformationDao;

    private PushMessageInformation pushMessageInformation;

    @Override
    protected void specificSetup() {
        pushMessageInformation =
                pushMessageMetricsService.storeNewRequestFrom(
                        "123",
                        "hello",
                        "127.0.01",
                        "testcase",
                        1
                );

        VariantMetricInformation variantMetricInformation = new VariantMetricInformation();
        variantMetricInformation.setVariantID("321");
        pushMessageInformation.addVariantInformations(variantMetricInformation);
        pushMessageMetricsService.updatePushMessageInformation(pushMessageInformation);
    }

    @Test
    public void updateAnalyticsTest() {
        pushMessageMetricsService.updateAnalytics(pushMessageInformation.getId(), "321");
        PushMessageInformation updatedPushInformation = pushMessageMetricsService.getPushMessageInformation(pushMessageInformation.getId());
        assertThat(updatedPushInformation.getAppOpenCounter()).isEqualTo(1);
        VariantMetricInformation updatedVariantMetric = variantMetricInformationDao.findVariantMetricInformationByVariantID("321", updatedPushInformation.getId());
        assertThat(updatedVariantMetric.getVariantOpenCounter()).isEqualTo(1);

        pushMessageMetricsService.updateAnalytics(pushMessageInformation.getId(), "321");
        PushMessageInformation updatedPushInformation1 = pushMessageMetricsService.getPushMessageInformation(pushMessageInformation.getId());
        assertThat(updatedPushInformation1.getAppOpenCounter()).isEqualTo(2);
        VariantMetricInformation updatedVariantMetric1 = variantMetricInformationDao.findVariantMetricInformationByVariantID("321", updatedPushInformation.getId());
        assertThat(updatedVariantMetric1.getVariantOpenCounter()).isEqualTo(2);

    }

    @Test
    public void deleteAnalyticsTest() {

        assertThat(pushMessageMetricsService.countMessagesForVariant("321")).isEqualTo(1);

        System.setProperty(PushMessageMetricsService.AEROGEAR_METRICS_STORAGE_MAX_DAYS, "0");

        // delete all
        pushMessageMetricsService.deleteOutdatedPushInformationData();

        assertThat(pushMessageMetricsService.countMessagesForVariant("321")).isZero();

    }
}