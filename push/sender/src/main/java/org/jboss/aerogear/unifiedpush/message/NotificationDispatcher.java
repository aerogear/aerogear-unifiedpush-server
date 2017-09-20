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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.token.TokenLoader;
import org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.context.ApplicationContext;

import reactor.core.publisher.WorkQueueProcessor;

/**
 * Receives a request for dispatching push notifications to specified devices from {@link TokenLoader}
 */
@Service
public class NotificationDispatcher {

    private final Logger logger = LoggerFactory.getLogger(NotificationDispatcher.class);

    @Inject
    private ApplicationContext context;

    @Inject
    private IPushMessageMetricsService pushMessageMetricsService;

	@Inject
	private WorkQueueProcessor<MessageHolderWithTokens> messages;

	@PostConstruct
	public void subscribe() {
		messages.repeat().subscribe(m -> sendMessagesToPushNetwork(m));
	}
    /**
     * Receives a {@link UnifiedPushMessage} and list of device tokens that the message should be sent to, selects appropriate sender implementation that
     * the push notifications are submitted to.
     *
     * @param msg object containing details about the payload and the related device tokens
     */
    public void sendMessagesToPushNetwork(MessageHolderWithTokens msg) {
        final Variant variant = msg.getVariant();
        final UnifiedPushMessage unifiedPushMessage = msg.getUnifiedPushMessage();
        final Collection<String> deviceTokens = msg.getDeviceTokens();

        logger.info(String.format("Received UnifiedPushMessage from queue, will now trigger the Push Notification delivery for the %s variant (%s)", variant.getType().getTypeName(), variant.getVariantID()));

        try {
        	// Any Unhandled exception will break this Flux stream
    		BeanFactoryAnnotationUtils.qualifiedBeanOfType(
    				context.getAutowireCapableBeanFactory(), PushNotificationSender.class, variant.getType().name())
			    		.sendPushMessage(variant, deviceTokens, unifiedPushMessage, msg.getPushMessageInformation().getId(),
			                    new SenderServiceCallback(
			                            variant,
			                            deviceTokens.size(),
			                            msg.getPushMessageInformation()
			                    )
			    		);
        } catch (Throwable e) {
        	logger.error("Unable to send push notification for %s variant ", variant.getName());
        	// TODO - implement retry policy
		}
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
            logger.debug(String.format("Sent '%s' message to '%d' devices", variant.getType().getTypeName(), tokenSize));
        }

        @Override
        public void onError(final String reason) {
            logger.warn(String.format("Error on '%s' delivery: %s", variant.getType().getTypeName(), reason));
            pushMessageMetricsService.appendError(pushMessageInformation, variant, reason);
        }
    }
}
