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

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.event.MetricsProcessingStartedEvent;
import org.jboss.aerogear.unifiedpush.message.event.TriggerMetricCollectionEvent;
import org.jboss.aerogear.unifiedpush.message.event.TriggerVariantMetricCollectionEvent;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Receives {@link TriggerVariantMetricCollectionEvent}s and turns them into one {@link TriggerMetricCollectionEvent}.
 *
 * Since {@link TriggerMetricCollectionEvent} should be send exactly once, it maintains set of IDs that metrics processing was started for.
 * It also rechecks in DB whether metrics processing already started.
 *
 * When metrics processing is starting, it broadcast that information via a topic to nodes in the cluster.
 */
@Stateless
public class MetricCollectionTrigger {

    private final Logger logger = LoggerFactory.getLogger(MetricCollectionTrigger.class);

    /**
     * Stores pushMessageInformationIds for the push messages that the metrics collection process was already started for
     */
    private static final Set<String> METRICS_PROCESSING_STARTED_FOR_IDS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Inject @DispatchToQueue
    private Event<MetricsProcessingStartedEvent> broadcastMetricsProcessingStarted;

    @Inject @DispatchToQueue
    private Event<TriggerMetricCollectionEvent> triggerMetricCollection;

    @Inject
    private PushMessageMetricsService metricsService;

    /**
     * Receives {@link TriggerVariantMetricCollectionEvent} event from JMS and determines whether a {@link TriggerMetricCollectionEvent} should be sent as reaction or not.
     *
     * It check with shared Set of IDs that metrics processing already started for, and if no record is there, it rechecks in DB.
     *
     * @param event the event dequeued from JMS
     */
    public void tryToStartMetricCollection(@Observes @Dequeue TriggerVariantMetricCollectionEvent event) {
        final String pushMessageInformationId = event.getPushMessageInformationId();

        if (!METRICS_PROCESSING_STARTED_FOR_IDS.contains(pushMessageInformationId)) {
            if (detectMetricsProcessingStartedFromDB(pushMessageInformationId)) {
                logger.debug(String.format("Detected that metrics collection already started from DB state for push message %s", pushMessageInformationId));
                METRICS_PROCESSING_STARTED_FOR_IDS.add(pushMessageInformationId);
            } else {
                if (!METRICS_PROCESSING_STARTED_FOR_IDS.contains(pushMessageInformationId)) { // re-check after DB read
                    METRICS_PROCESSING_STARTED_FOR_IDS.add(pushMessageInformationId);
                    logger.debug(String.format("Broadcasting information that metrics processing started for push message %s", pushMessageInformationId));
                    broadcastMetricsProcessingStarted.fire(new MetricsProcessingStartedEvent(pushMessageInformationId));
                    logger.debug(String.format("Trigger metric collection process for push message %s", pushMessageInformationId));
                    triggerMetricCollection.fire(new TriggerMetricCollectionEvent(pushMessageInformationId));
                }
            }
        }
    }

    private boolean detectMetricsProcessingStartedFromDB(String pushMessageInformationId) {
        PushMessageInformation pmi = metricsService.getPushMessageInformation(pushMessageInformationId);
        if (pmi.getServedVariants() > 0) {
            return true;
        }
        for (VariantMetricInformation vmi : pmi.getVariantInformations()) {
            if (vmi.getServedBatches() > 0 || vmi.getTotalBatches() > 0) {
                return true;
            }
        }
        return false;
    }

    public void markMetricsProcessingAsStarted(@Observes @Dequeue MetricsProcessingStartedEvent event) throws JMSException {
        logger.debug(String.format("Received signal that metrics collection started for push message %s", event.getPushMessageInformationId()));
        METRICS_PROCESSING_STARTED_FOR_IDS.add(event.getPushMessageInformationId());
    }
}
