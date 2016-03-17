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
package org.jboss.aerogear.unifiedpush.message.jms;

import javax.annotation.Resource;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.Topic;

import org.jboss.aerogear.unifiedpush.message.event.AllBatchesLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.event.BatchLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.event.MetricsProcessingStarted;
import org.jboss.aerogear.unifiedpush.message.event.TriggerMetricCollection;
import org.jboss.aerogear.unifiedpush.message.event.TriggerVariantMetricCollection;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;

/**
 * A CDI-to-JMS bridge takes some selected CDI events and passes them to JMS messaging system so that they can be handled asynchronously.
 */
public class CdiJmsBridge {

    @Resource(mappedName = "java:/queue/AllBatchesLoadedQueue")
    private Queue allBatchesLoadedQueue;

    @Resource(mappedName = "java:/queue/BatchLoadedQueue")
    private Queue batchLoadedQueue;

    @Resource(mappedName = "java:/queue/TriggerMetricCollectionQueue")
    private Queue triggerMetricCollectionQueue;

    @Resource(mappedName = "java:/queue/TriggerVariantMetricCollectionQueue")
    private Queue triggerVariantMetricCollectionQueue;

    @Resource(mappedName = "java:/topic/MetricsProcessingStartedTopic")
    private Topic metricsProcessingStartedTopic;

    @Inject
    private JmsClient jmsClient;

    public void queueMessage(@Observes @DispatchToQueue AllBatchesLoadedEvent msg) {
        jmsClient.send(msg)
            .inTransaction()
            .withProperty("variantID", msg.getVariantID())
            .to(allBatchesLoadedQueue);
    }

    public void queueMessage(@Observes @DispatchToQueue BatchLoadedEvent msg) {
        jmsClient.send(msg)
            .inTransaction()
            .withProperty("variantID", msg.getVariantID())
            .to(batchLoadedQueue);
    }

    public void queueMessage(@Observes @DispatchToQueue TriggerMetricCollection msg) {
    	jmsClient.send(msg)
    	    .withDuplicateDetectionId(msg.getPushMessageInformationId())
    	    .withDelayedDelivery(500L)
    	    .to(triggerMetricCollectionQueue);
    }

    public void queueMessage(@Observes @DispatchToQueue TriggerVariantMetricCollection msg) {
        jmsClient.send(msg)
            .to(triggerVariantMetricCollectionQueue);
    }

    public void broadcastMessage(@Observes @DispatchToQueue MetricsProcessingStarted msg) {
        jmsClient.send(msg)
            .to(metricsProcessingStartedTopic);
    }
}
