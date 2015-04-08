package org.jboss.aerogear.unifiedpush.message;

import java.util.List;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

public class Sender {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(Sender.class);

    @Inject
    @Any
    private Instance<PushNotificationSender> senders;

    @Inject
    @DispatchToQueue
    private Event<VariantMetricInformation> dispatchVariantMetricEvent;

    public void sendMessagesToPushNetwork(@Observes @Dequeue MessageWithTokens msg) {
        final Variant variant = msg.getVariant();
        final UnifiedPushMessage message = msg.getUnifiedPushMessage();
        final List<String> deviceTokens = msg.getDeviceTokens();

        logger.fine("Received message from queue: " + msg.getUnifiedPushMessage().getMessage().getAlert());

        senders.select(new SenderTypeLiteral(variant.getClass())).get()
                            .sendPushMessage(variant, deviceTokens, message, new SenderServiceCallback(variant, deviceTokens.size(), msg.getPushMessageInformation()));
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
    private void updateStatusOfPushMessageInformation(final PushMessageInformation pushMessageInformation, final String variantID, final int receivers, final Boolean deliveryStatus) {
        this.updateStatusOfPushMessageInformation(pushMessageInformation, variantID, receivers, deliveryStatus, null);
    }

    private void updateStatusOfPushMessageInformation(final PushMessageInformation pushMessageInformation, final String variantID, final int receivers, final Boolean deliveryStatus, final String reason) {
        final VariantMetricInformation variantMetricInformation = new VariantMetricInformation();
        variantMetricInformation.setPushMessageInformation(pushMessageInformation);
        variantMetricInformation.setVariantID(variantID);
        variantMetricInformation.setReceivers(receivers);
        variantMetricInformation.setDeliveryStatus(deliveryStatus);
        variantMetricInformation.setReason(reason);
        dispatchVariantMetricEvent.fire(variantMetricInformation);
    }
}