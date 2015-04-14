package org.jboss.aerogear.unifiedpush.message;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

@Stateless
public class MetricCollector {

    @Inject
    private PushMessageMetricsService metricsService;

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