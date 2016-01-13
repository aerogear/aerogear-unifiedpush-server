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
import javax.jms.JMSException;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.message.event.AllBatchesLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.event.BatchLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.event.PushMessageCompletedEvent;
import org.jboss.aerogear.unifiedpush.message.event.TriggerMetricCollection;
import org.jboss.aerogear.unifiedpush.message.event.VariantCompletedEvent;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestMetricsCollector extends AbstractJMSTest {

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestMetricsCollector.class)
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

    @Resource(mappedName = "java:/queue/MetricsQueue")
    private Queue metricsQueue;

    private String pushMessageInformationId = UUID.randomUUID().toString();
    private String variantID1 = UUID.randomUUID().toString();
    private String variantID2 = UUID.randomUUID().toString();

    private static final CountDownLatch pushMessagesCompleted = new CountDownLatch(1);
    private static final CountDownLatch variantsCompleted = new CountDownLatch(2);

    @Test
    public void test(PushMessageInformationDao pushMessageInformationDao) throws InterruptedException, JMSException {
        // given
        PushMessageInformation pushMetric = new PushMessageInformation();
        pushMetric.setId(pushMessageInformationId);
        VariantMetricInformation variant1Metric1 = new VariantMetricInformation();
        variant1Metric1.setPushMessageInformation(pushMetric);
        variant1Metric1.setVariantID(variantID1);
        variant1Metric1.setServedBatches(1);
        VariantMetricInformation variant1Metric2 = new VariantMetricInformation();
        variant1Metric2.setPushMessageInformation(pushMetric);
        variant1Metric2.setVariantID(variantID1);
        variant1Metric2.setServedBatches(1);
        VariantMetricInformation variant2Metric1 = new VariantMetricInformation();
        variant2Metric1.setPushMessageInformation(pushMetric);
        variant2Metric1.setVariantID(variantID2);
        variant2Metric1.setServedBatches(1);
        when(pushMessageInformationDao.find(pushMetric.getId())).thenReturn(pushMetric);

        // when
        send(new BatchLoadedEvent(variantID1+":"+pushMetric.getId())).withProperty("variantID", variantID1+":"+pushMetric.getId()).to(batchLoadedQueue);
        send(new BatchLoadedEvent(variantID1+":"+pushMetric.getId())).withProperty("variantID", variantID1+":"+pushMetric.getId()).to(batchLoadedQueue);
        send(new BatchLoadedEvent(variantID2+":"+pushMetric.getId())).withProperty("variantID", variantID2+":"+pushMetric.getId()).to(batchLoadedQueue);
        send(new AllBatchesLoadedEvent(variantID1+":"+pushMetric.getId())).withProperty("variantID", variantID1+":"+pushMetric.getId()).to(allBatchesLoaded);
        send(new AllBatchesLoadedEvent(variantID2+":"+pushMetric.getId())).withProperty("variantID", variantID2+":"+pushMetric.getId()).to(allBatchesLoaded);

        send(variant1Metric1).withProperty("pushMessageInformationId", pushMessageInformationId).to(metricsQueue);
        send(variant1Metric2).withProperty("pushMessageInformationId", pushMessageInformationId).to(metricsQueue);
        send(variant2Metric1).withProperty("pushMessageInformationId", pushMessageInformationId).to(metricsQueue);

        metricsCollector.collectMetrics(new TriggerMetricCollection(pushMessageInformationId));

        variantsCompleted.await(2, TimeUnit.SECONDS);
        pushMessagesCompleted.await(1, TimeUnit.SECONDS);

        // then
        assertEquals(2, pushMetric.getServedVariants().intValue());
        assertEquals(2, variant1Metric1.getServedBatches().intValue());
        assertEquals(2, variant1Metric1.getTotalBatches().intValue());
        assertEquals(1, variant2Metric1.getServedBatches().intValue());
        assertEquals(1, variant2Metric1.getTotalBatches().intValue());
        assertNull(receive().withTimeout(100).withSelector("variantID = '%s'", variantID1+":"+pushMetric.getId()).from(batchLoadedQueue));
        assertNull(receive().withTimeout(100).withSelector("variantID = '%s'", variantID1+":"+pushMetric.getId()).from(allBatchesLoaded));
        assertNull(receive().withTimeout(100).withSelector("variantID = '%s'", variantID2+":"+pushMetric.getId()).from(batchLoadedQueue));
        assertNull(receive().withTimeout(100).withSelector("variantID = '%s'", variantID2+":"+pushMetric.getId()).from(allBatchesLoaded));
    }

    public void observeVariantCompleted(@Observes VariantCompletedEvent variantCompleted) {
        variantsCompleted.countDown();
    }

    public void observePushMessageCompleted(@Observes PushMessageCompletedEvent pushMessageCompleted) {
        pushMessagesCompleted.countDown();
    }

}
