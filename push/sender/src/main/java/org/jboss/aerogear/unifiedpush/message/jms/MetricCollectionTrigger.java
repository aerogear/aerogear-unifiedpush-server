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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.event.MetricsProcessingStarted;
import org.jboss.aerogear.unifiedpush.message.event.TriggerMetricCollection;
import org.jboss.aerogear.unifiedpush.message.event.TriggerVariantMetricCollection;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@Stateless
public class MetricCollectionTrigger {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(MetricCollectionTrigger.class);

    private static final Set<String> METRICS_PROCESSING_STARTED = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Inject @DispatchToQueue
    private Event<MetricsProcessingStarted> broadcastMetricsProcessingStarted;

    @Inject @DispatchToQueue
    private Event<TriggerMetricCollection> triggerMetricCollection;

    @Inject
    private PushMessageMetricsService metricsService;

    public void tryToStartMetricCollection(@Observes @Dequeue TriggerVariantMetricCollection event) throws JMSException {
        final String pushMessageInformationId = event.getPushMessageInformationId();

        if (!METRICS_PROCESSING_STARTED.contains(pushMessageInformationId)) {
            if (detectMetricsProcessingStartedFromDB(pushMessageInformationId)) {
                logger.fine(String.format("Detected that metrics collection already started from DB state for push message %s", pushMessageInformationId));
                METRICS_PROCESSING_STARTED.add(pushMessageInformationId);
            } else {
                if (!METRICS_PROCESSING_STARTED.contains(pushMessageInformationId)) { // re-check after DB read
                    METRICS_PROCESSING_STARTED.add(pushMessageInformationId);
                    logger.fine(String.format("Broadcasting information that metrics processing started for push message %s", pushMessageInformationId));
                    broadcastMetricsProcessingStarted.fire(new MetricsProcessingStarted(pushMessageInformationId));
                    logger.fine(String.format("Trigger metric collection process for push message %s", pushMessageInformationId));
                    triggerMetricCollection.fire(new TriggerMetricCollection(pushMessageInformationId));
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

    public void markMetricsProcessingAsStarted(@Observes @Dequeue MetricsProcessingStarted event) throws JMSException {
        logger.fine(String.format("Received signal that metrics collection started for push message %s", event.getPushMessageInformationId()));
        METRICS_PROCESSING_STARTED.add(event.getPushMessageInformationId());
    }
}
