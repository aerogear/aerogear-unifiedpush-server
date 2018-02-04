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

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.kafka.Dequeue;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.aerogear.kafka.SimpleKafkaProducer;
import org.aerogear.kafka.cdi.annotation.Producer;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import java.util.Collection;

/**
 * Receives a request for dispatching push notifications to specified devices from
 * {@link org.jboss.aerogear.unifiedpush.message.kafka.MessageHolderWithTokensKafkaConsumer} and selects
 * the appropriate sender network
 */
@Stateless
@Deprecated
public class NotificationDispatcher {

    private final Logger logger = LoggerFactory.getLogger(NotificationDispatcher.class);

    /**
     * Topic to which a "success" message will be sent if a push message was successfully send and "failure" message otherwise.
     */
    public static final String KAFKA_PUSH_DELIVERY_METRICS_TOPIC = "agpush_pushDeliveryMetrics";
    public static final String KAFKA_METRICS_ON_DELIVERY_SUCCESS = "agpush_deliverySuccess";
    public static final String KAFKA_METRICS_ON_DELIVERY_FAILURE = "agpush_deliveryFailure";
    
    @Inject
    @Any
    private Instance<PushNotificationSender> senders;

    @Inject
    private PushMessageMetricsService pushMessageMetricsService;
   
    @Producer
    private SimpleKafkaProducer<String, String> pushDeliveryMetricsProducer;

    /**
     * Receives a {@link UnifiedPushMessage} and list of device tokens that the message should be sent to, selects appropriate sender implementation that
     * the push notifications are submitted to.
     *
     * @param msg object containing details about the payload and the related device tokens
     */
    public void sendMessagesToPushNetwork(@Observes @Dequeue MessageHolderWithTokens msg) {
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

    /**
     * Implementation of the {@link NotificationSenderCallback} interface for specific
     * push networks with additional fields for variant, token size and flat push message
     * information
     */
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
            // add to a Kafka topic that one more message was sent successfully
            pushDeliveryMetricsProducer.send(KAFKA_PUSH_DELIVERY_METRICS_TOPIC, pushMessageInformation.getId(), KAFKA_METRICS_ON_DELIVERY_SUCCESS);
            logger.debug("Sent {} message to {} devices", variant.getType().getTypeName(), tokenSize);
        }

        @Override
        public void onError(final String reason) {
            logger.warn("Error on '{}' delivery: {}", variant.getType().getTypeName(), reason);
            pushMessageMetricsService.appendError(pushMessageInformation, variant, reason);
            // add to a Kafka topic that a message was sent unsuccessfully
            pushDeliveryMetricsProducer.send(KAFKA_PUSH_DELIVERY_METRICS_TOPIC, pushMessageInformation.getId(), KAFKA_METRICS_ON_DELIVERY_FAILURE);
        }
    }
    
    public SimpleKafkaProducer<String, String> getPushDeliveryMetricsProducer(){
        return pushDeliveryMetricsProducer;
    }

}
