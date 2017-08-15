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
package org.jboss.aerogear.unifiedpush.message.kafka;

import net.wessendorf.kafka.cdi.annotation.Consumer;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;

public class MessageHolderWithTokensKafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(MessageHolderWithTokensKafkaConsumer.class);

    private final String ADM_TOPIC = "AdmToken_Topic";

    private final String APNS_TOPIC = "APNsToken_Topic";

    private final String FCM_TOPIC = "FCMToken_Topic";

    private final String MPNS_TOPIC = "MPNSToken_Topic";

    private final String MOZ_TOPIC = "SimplePushToken_Topic";

    private final String WNS_TOPIC = "WNSToken_Topic";

    @Inject
    @Dequeue
    private Event<MessageHolderWithTokens> dequeueEvent;

    @Consumer(topics = {ADM_TOPIC, APNS_TOPIC, FCM_TOPIC, MPNS_TOPIC, MOZ_TOPIC, WNS_TOPIC}, groupId = "MessageHolderWithTokensKafkaConsumer_group")
    public void listener(final MessageHolderWithTokens msg) {

        logger.info("receiving tokens from topic, triggering Notification Dispatcher to pick the right sender");
        dequeueEvent.fire(msg);
    }
}