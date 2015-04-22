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

import java.util.Collection;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

/**
 * Receives a request for dispatching push notifications to specified devices from {@link TokenLoader}
 *
 * and generates metrics that are sent for further processing to {@link MetricsCollector}.
 */
@Stateless
public class NotificationDispatcher {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(NotificationDispatcher.class);

    @Inject
    @Any
    private Instance<PushNotificationSender> senders;

    @Inject
    @DispatchToQueue
    private Event<VariantMetricInformation> dispatchVariantMetricEvent;

    /**
     * Receives a {@link UnifiedPushMessage} and list of device tokens that the message should be sent to, selects appropriate sender implementation that
     * the push notifications are submitted to.
     *
     * Once the sending process finishes, generates message for {@link MetricsCollector} with information how much devices was the notification submitted to.
     */
    public void sendMessagesToPushNetwork(@Observes @Dequeue MessageHolderWithTokens msg) {
        final Variant variant = msg.getVariant();
        final UnifiedPushMessage message = msg.getUnifiedPushMessage();
        final Collection<String> deviceTokens = msg.getDeviceTokens();

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

    private void updateStatusOfPushMessageInformation(final PushMessageInformation pushMessageInformation,
            final String variantID, final int receivers, final Boolean deliveryStatus, final String reason) {
        final VariantMetricInformation variantMetricInformation = new VariantMetricInformation();
        variantMetricInformation.setPushMessageInformation(pushMessageInformation);
        variantMetricInformation.setVariantID(variantID);
        variantMetricInformation.setReceivers(receivers);
        variantMetricInformation.setDeliveryStatus(deliveryStatus);
        variantMetricInformation.setReason(reason);
        dispatchVariantMetricEvent.fire(variantMetricInformation);
    }
}