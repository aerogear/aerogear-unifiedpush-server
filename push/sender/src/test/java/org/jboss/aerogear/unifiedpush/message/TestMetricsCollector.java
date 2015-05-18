/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.message.holder.AllBatchesLoaded;
import org.jboss.aerogear.unifiedpush.message.holder.BatchLoaded;
import org.jboss.aerogear.unifiedpush.message.holder.PushMessageCompleted;
import org.jboss.aerogear.unifiedpush.message.holder.VariantCompleted;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestMetricsCollector extends AbstractJMSTest {

    @Deployment
    public static WebArchive archive() {
        return ShrinkWrap
                .create(UnifiedPushArchive.class)
                .withMessaging()
                    .addClasses(MetricsCollector.class)
                    .addClasses(PushMessageMetricsService.class)
                .withMockito()
                    .addClasses(MockProviders.class)
                .as(WebArchive.class);
    }

    @Inject
    private MetricsCollector metricsCollector;

    @Resource(mappedName = "java:/queue/BatchLoadedQueue")
    private Queue batchLoadedQueue;

    @Resource(mappedName = "java:/queue/AllBatchesLoadedQueue")
    private Queue allBatchesLoaded;

    private String variantID1 = UUID.randomUUID().toString();
    private String variantID2 = UUID.randomUUID().toString();

    private static final CountDownLatch pushMessagesCompleted = new CountDownLatch(1);
    private static final CountDownLatch variantsCompleted = new CountDownLatch(2);

    @Test
    public void test(PushMessageInformationDao pushMessageInformationDao) throws InterruptedException {
        // given
        PushMessageInformation pushMetric = new PushMessageInformation();
        VariantMetricInformation variant1Metric1 = new VariantMetricInformation();
        variant1Metric1.setPushMessageInformation(pushMetric);
        variant1Metric1.setVariantID(variantID1);
        VariantMetricInformation variant1Metric2 = new VariantMetricInformation();
        variant1Metric2.setPushMessageInformation(pushMetric);
        variant1Metric2.setVariantID(variantID1);
        VariantMetricInformation variant2Metric1 = new VariantMetricInformation();
        variant2Metric1.setPushMessageInformation(pushMetric);
        variant2Metric1.setVariantID(variantID2);
        when(pushMessageInformationDao.refresh(pushMetric)).thenReturn(pushMetric);

        // when
        send(batchLoadedQueue, new BatchLoaded(variantID1), variantID1);
        send(batchLoadedQueue, new BatchLoaded(variantID1), variantID1);
        send(batchLoadedQueue, new BatchLoaded(variantID2), variantID2);
        send(allBatchesLoaded, new AllBatchesLoaded(variantID1), variantID1);
        send(allBatchesLoaded, new AllBatchesLoaded(variantID2), variantID2);

        metricsCollector.collectMetrics(variant1Metric1);
        metricsCollector.collectMetrics(variant1Metric2);
        metricsCollector.collectMetrics(variant2Metric1);

        pushMessagesCompleted.await(1, TimeUnit.SECONDS);
        variantsCompleted.await(1, TimeUnit.SECONDS);

        // then
        assertEquals(2, pushMetric.getServedVariants());
        assertEquals(2, variant1Metric1.getServedBatches());
        assertEquals(2, variant1Metric1.getTotalBatches());
        assertEquals(1, variant2Metric1.getServedBatches());
        assertEquals(1, variant2Metric1.getTotalBatches());
        assertNull(receive(batchLoadedQueue, variantID1));
        assertNull(receive(allBatchesLoaded, variantID1));
        assertNull(receive(batchLoadedQueue, variantID2));
        assertNull(receive(allBatchesLoaded, variantID2));
    }

    public void observeVariantCompleted(@Observes VariantCompleted variantCompleted) {
        variantsCompleted.countDown();
    }

    public void observePushMessageCompleted(@Observes PushMessageCompleted pushMessageCompleted) {
        pushMessagesCompleted.countDown();
    }

}
