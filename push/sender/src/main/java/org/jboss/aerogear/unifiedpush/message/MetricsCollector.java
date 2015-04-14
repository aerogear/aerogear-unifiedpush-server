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
package org.jboss.aerogear.unifiedpush.message;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

/**
 * Receives metrics from {@link NotificationDispatcher} and updates the database.
 */
@Stateless
public class MetricsCollector {

    @Inject
    private PushMessageMetricsService metricsService;

    /**
     * Receives metrics and update the push message information in database.
     */
    public void collectMetrics(@Observes @Dequeue VariantMetricInformation variantMetricInformation) {
        PushMessageInformation pushMessageInformation = variantMetricInformation.getPushMessageInformation();

        metricsService.updatePushMessageInformation(pushMessageInformation);

        boolean updatedExisting = false;
        for (VariantMetricInformation existingMetric : pushMessageInformation.getVariantInformations()) {
            if (variantMetricInformation.getVariantID().equals(existingMetric.getVariantID())) {
                updatedExisting = true;
                updateExistingMetric(existingMetric, variantMetricInformation);
            }
        }
        if (!updatedExisting) {
            pushMessageInformation.addVariantInformations(variantMetricInformation);
        }

        metricsService.updatePushMessageInformation(pushMessageInformation);
    }

    private void updateExistingMetric(VariantMetricInformation existing, VariantMetricInformation update) {
        existing.setReceivers(existing.getReceivers() + update.getReceivers());
        if (existing.getDeliveryStatus() == null) {
            existing.setDeliveryStatus(update.getDeliveryStatus());
        }
        if (existing.getDeliveryStatus() == Boolean.TRUE && update.getDeliveryStatus() == Boolean.FALSE) {
            existing.setDeliveryStatus(Boolean.FALSE);
        }
        if (existing.getReason() == null && update.getReason() != null) {
            existing.setReason(update.getReason());
        }
    }
}