package org.jboss.aerogear.unifiedpush.message;

import java.util.List;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

@MessageDriven(name = "TokenBatchQueue", activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/TokenBatchQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
public class SenderMDB implements MessageListener {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(SenderMDB.class);

    @Inject
    @Any
    private Instance<PushNotificationSender> senders;

//    @Inject
//    private PushMessageMetricsService metricsService;

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(javax.jms.Message jmsMessage) {
        try {
            if (jmsMessage instanceof ObjectMessage) {
                MessageForTokens msg = (MessageForTokens) ((ObjectMessage) jmsMessage).getObject();
                Variant variant = msg.getVariant();
                UnifiedPushMessage message = msg.getUnifiedPushMessage();
                List<String> deviceTokens = msg.getDeviceTokens();

                senders.select(new SenderTypeLiteral(variant.getClass())).get()
                                    .sendPushMessage(variant, deviceTokens, message, new SenderServiceCallback(variant, deviceTokens.size(), null));

                logger.fine("Received message from queue: " + msg.getUnifiedPushMessage().getMessage().getAlert());
            } else {
                logger.warning("Received message of wrong type: " + jmsMessage.getClass().getName());
            }
        } catch (JMSException e) {
            throw new MessageDeliveryException("Failed to handle message from TokenBatchQueue", e);
        }
    }

    private class SenderServiceCallback implements NotificationSenderCallback {
        private final Variant variant;
        private final int tokenSize;
        private final PushMessageInformation pushMessageInformation;

        public SenderServiceCallback(Variant variant, int tokenSize, PushMessageInformation pushMessageInformation) {
            this.variant = variant;
            this.tokenSize = tokenSize;
            this.pushMessageInformation = pushMessageInformation;
        }

        @Override
        public void onSuccess() {
            logger.fine(String.format("Sent '%s' message to '%d' devices", variant.getType().getTypeName(), tokenSize));
            updateStatusOfPushMessageInformation(pushMessageInformation, variant.getVariantID(), tokenSize, Boolean.TRUE);
        }

        @Override
        public void onError(final String reason) {
            logger.warning(String.format("Error on '%s' delivery", variant.getType().getTypeName()));
            updateStatusOfPushMessageInformation(pushMessageInformation, variant.getVariantID(), tokenSize, Boolean.FALSE, reason);
        }
    }

    /**
     * Helpers to update the given {@link PushMessageInformation} with a {@link VariantMetricInformation} object
     */
    private void updateStatusOfPushMessageInformation(final PushMessageInformation pushMessageInformation, final String variantID, final int receives, final Boolean deliveryStatus) {
        this.updateStatusOfPushMessageInformation(pushMessageInformation, variantID, receives, deliveryStatus, null);
    }

    private void updateStatusOfPushMessageInformation(final PushMessageInformation pushMessageInformation, final String variantID, final int receives, final Boolean deliveryStatus, final String reason) {
//        final VariantMetricInformation variantMetricInformation = new VariantMetricInformation();
//        variantMetricInformation.setVariantID(variantID);
//        variantMetricInformation.setReceivers(receives);
//        variantMetricInformation.setDeliveryStatus(deliveryStatus);
//        variantMetricInformation.setReason(reason);
//        pushMessageInformation.addVariantInformations(variantMetricInformation);
//        pushMessageInformation.setTotalReceivers(pushMessageInformation.getTotalReceivers() + receives);
//
//        // store it!
//        metricsService.updatePushMessageInformation(pushMessageInformation);
    }
}