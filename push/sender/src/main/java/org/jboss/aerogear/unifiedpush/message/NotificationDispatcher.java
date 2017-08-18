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

import net.wessendorf.kafka.cdi.annotation.Consumer;
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.message.token.TokenLoader;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Receives a request for dispatching push notifications to specified devices from {@link TokenLoader}
 */
@Stateless
public class NotificationDispatcher {

    private final Logger logger = LoggerFactory.getLogger(NotificationDispatcher.class);

    @Inject
    @Any
    private Instance<PushNotificationSender> senders;

    @Inject
    private PushMessageMetricsService pushMessageMetricsService;

    private final String ADM_TOPIC = "agpush_AdmTokenTopic";

    private final String APNS_TOPIC = "agpush_APNsTokenTopic";

    private final String FCM_TOPIC = "agpush_FCMTokenTopic";

    private final String MPNS_TOPIC = "agpush_MPNSTokenTopic";

    private final String MOZ_TOPIC = "agpush_SimplePushTokenTopic";

    private final String WNS_TOPIC = "agpush_WNSTokenTopic";

    /**
     * Consumes a record of type {@link UnifiedPushMessage} with a list of device tokens that the message should
     * be sent to, and selects the appropriate sender implementation that the push notifications
     * are submitted to.
     *
     * @param msg object containing details about the payload and the related device tokens
     */
    @Consumer(topics = {ADM_TOPIC, APNS_TOPIC, FCM_TOPIC, MPNS_TOPIC, MOZ_TOPIC, WNS_TOPIC}, groupId = "NotificationDispatcherKafkaConsumer_group")
    public void sendMessagesToPushNetwork(final MessageHolderWithTokens msg) {
        final Variant variant = msg.getVariant();
        final UnifiedPushMessage unifiedPushMessage = msg.getUnifiedPushMessage();
        final Collection<String> deviceTokens = msg.getDeviceTokens();

        logger.info("UnifiedPushMessage was successfully received. Push Notification delivery for the {} variant ({}) will now be triggered.", variant.getType().getTypeName(), variant.getVariantID());

        senders.select(new SenderTypeLiteral(variant.getType())).get()
                .sendPushMessage(variant, deviceTokens, unifiedPushMessage, msg.getPushMessageInformation().getId(),
                        new SenderServiceCallback(
                                variant,
                                deviceTokens.size(),
                                msg.getPushMessageInformation()
                        )
                );
    }

    private class SenderServiceCallback implements NotificationSenderCallback {
        private final Variant variant;
        private final int tokenSize;
        private final FlatPushMessageInformation pushMessageInformation;

        public SenderServiceCallback(Variant variant, int tokenSize, FlatPushMessageInformation pushMessageInformation) {
            this.variant = variant;
            this.tokenSize = tokenSize;
            this.pushMessageInformation = pushMessageInformation;
        }

        @Override
        public void onSuccess() {
            logger.debug("Sent '{}' message to '{}' devices", variant.getType().getTypeName(), tokenSize);
        }

        @Override
        public void onError(final String reason) {
            logger.warn("Error on '{}' delivery: {}", variant.getType().getTypeName(), reason);
            pushMessageMetricsService.appendError(pushMessageInformation, variant, reason);
        }
    }
}