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
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.message.token.TokenLoader;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;
import org.jboss.aerogear.unifiedpush.message.util.QueueUtils;
import org.jboss.aerogear.unifiedpush.service.metrics.PrometheusExporter;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Receives a request for dispatching push notifications to specified devices from {@link TokenLoader}
 */
@Stateless
public class NotificationDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDispatcher.class);

    private static int maxRetries = ConfigurationUtils.tryGetGlobalIntegerProperty("AMQ_MAX_RETRIES", 3);
    private static int retryTimeout = ConfigurationUtils.tryGetGlobalIntegerProperty("AMQ_BACKOFF_SECONDS", 10);

    @Inject
    @Any
    private Instance<PushNotificationSender> senders;

    @Inject
    private PushMessageMetricsService pushMessageMetricsService;

    @Inject
    private JmsClient jmsClient;

    

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

        logger.info("Received UnifiedPushMessage from JMS queue, will now trigger the Push Notification delivery for the {} variant ({})", variant.getType().getTypeName(), variant.getVariantID());
        String deduplicationId = String.format("%s-%s-%d", msg.getPushMessageInformation().getId(), msg.getSerialId(), msg.getRetryCount());
        logger.debug("Receiving message " + deduplicationId);

        try {
            senders.select(new SenderTypeLiteral(variant.getType())).get()
          .sendPushMessage(variant, deviceTokens, unifiedPushMessage, msg.getPushMessageInformation().getId(),
            new SenderServiceCallback(
              variant,
              deviceTokens.size(),
              msg.getPushMessageInformation()
            )
          );
          
        } catch (Exception everything) {
            //What this catch block does is makes sure that errors are always 
            // caught and never lead to a message we saw being requeued.
            logger.error("There was an uncaught exception.\n" +everything.getMessage(), everything);
            try {
                new SenderServiceCallback(
                    variant,
                    deviceTokens.size(),
                    msg.getPushMessageInformation()
                  ).onError(everything.getMessage());
                
                  int retryCount = msg.getRetryCount();
                  if (retryCount < maxRetries) {

                    MessageHolderWithTokens newMessage = new MessageHolderWithTokens(removeErrors(msg.getPushMessageInformation()), msg.getUnifiedPushMessage(), msg.getVariant(), msg.getDeviceTokens(), msg.getSerialId());
                    for (int i = 0; i < msg.getRetryCount() + 1; i++) {
                        newMessage.incrRetryCount();
                    }

                    final VariantType variantType = newMessage.getVariant().getType();
                    deduplicationId = String.format("%s-%s-%d", newMessage.getPushMessageInformation().getId(), newMessage.getSerialId(), newMessage.getRetryCount());
                    logger.debug("Sending retry message " + deduplicationId);

                    jmsClient.send(newMessage).withDelayedDelivery(retryTimeout * 1000l * newMessage.getRetryCount()).withDuplicateDetectionId(deduplicationId).to(QueueUtils.selectTokenQueue(variantType));
                  }
            } catch (Exception writeErrorException) {
                logger.error("There was a error writing the exception.\n" +writeErrorException.getMessage(), writeErrorException);
            }

        }

    }


    private FlatPushMessageInformation removeErrors(FlatPushMessageInformation pushMessageInformation) {
        FlatPushMessageInformation info = new FlatPushMessageInformation();
        info.setAppOpenCounter(pushMessageInformation.getAppOpenCounter());
        info.setClientIdentifier(pushMessageInformation.getClientIdentifier());
        info.setFirstOpenDate(pushMessageInformation.getFirstOpenDate());
        info.setId(pushMessageInformation.getId());
        info.setIpAddress(pushMessageInformation.getIpAddress());
        info.setLastOpenDate(pushMessageInformation.getLastOpenDate());
        info.setPushApplicationId(pushMessageInformation.getPushApplicationId());
        info.setRawJsonMessage(pushMessageInformation.getRawJsonMessage());
        info.setSubmitDate(pushMessageInformation.getSubmitDate());
        return info;
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
            PrometheusExporter.instance().increaseTotalPushRequestsFail();
            pushMessageMetricsService.appendError(pushMessageInformation, variant, reason);
        }
    }
}
