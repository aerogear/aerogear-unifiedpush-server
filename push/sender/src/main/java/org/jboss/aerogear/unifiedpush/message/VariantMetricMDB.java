package org.jboss.aerogear.unifiedpush.message;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@MessageDriven(name = "VariantMetricQueue", activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/VariantMetricQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class VariantMetricMDB implements MessageListener {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(VariantMetricMDB.class);

    @Inject
    private PushMessageMetricsService metricsService;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onMessage(javax.jms.Message jmsMessage) {
        try {
            if (jmsMessage instanceof ObjectMessage) {
                final VariantMetricInformation variantMetricInformation = (VariantMetricInformation) ((ObjectMessage) jmsMessage).getObject();
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
            } else {
                logger.warning("Received message of wrong type: " + jmsMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to handle message from TokenBatchQueue", e);
        }
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