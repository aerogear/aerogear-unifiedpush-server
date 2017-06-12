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

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@Stateless
public class VariantMetricInformationObserver {

    private final Logger logger = LoggerFactory.getLogger(VariantMetricInformationObserver.class);

    @Inject
    private PushMessageMetricsService metricsService;

    public void processVariantMetricInformation(@Observes @Dequeue VariantMetricInformation vmi) {

        final String pushMessageInformationId = vmi.getPushMessageInformation().getId();
        final PushMessageInformation pushMessageInformation = metricsService.getPushMessageInformation(pushMessageInformationId);
        metricsService.lock(pushMessageInformation);

        logger.error("transforming some VMIs....");

        // transformer:
        pushMessageInformation.setTotalReceivers(pushMessageInformation.getTotalReceivers() + vmi.getReceivers());
        boolean updatedExisting = false;
        for (VariantMetricInformation existingMetric : pushMessageInformation.getVariantInformations()) {
            if (vmi.getVariantID().equals(existingMetric.getVariantID())) {
                updatedExisting = true;
                updateExistingMetric(existingMetric, vmi);
                vmi = existingMetric;
                break;
            }
        }


        if (!updatedExisting) {
            pushMessageInformation.addVariantInformations(vmi);
        }
    }

    private void updateExistingMetric(VariantMetricInformation existing, VariantMetricInformation update) {
        existing.setReceivers(existing.getReceivers() + update.getReceivers());
        existing.setServedBatches(existing.getServedBatches() + update.getServedBatches());
        existing.setTotalBatches(existing.getTotalBatches() + update.getTotalBatches());
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
