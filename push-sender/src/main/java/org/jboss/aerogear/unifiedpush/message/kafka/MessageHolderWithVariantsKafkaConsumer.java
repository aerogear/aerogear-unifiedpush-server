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
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;


public class MessageHolderWithVariantsKafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(MessageHolderWithVariantsKafkaConsumer.class);

    private final String ADM_TOPIC = "agpush_AdmMessageTopic";

    private final String APNS_TOPIC = "agpush_APNsMessageTopic";

    private final String FCM_TOPIC = "agpush_FCMMessageTopic";

    private final String MPNS_TOPIC = "agpush_MPNSMessageTopic";

    private final String MOZ_TOPIC = "agpush_SimplePushMessageTopic";

    private final String WNS_TOPIC = "agpush_WNSMessageTopic";

    @Inject
    @Dequeue
    private Event<MessageHolderWithVariants> dequeueEvent;

    @Consumer(topics = {ADM_TOPIC, APNS_TOPIC, FCM_TOPIC, MPNS_TOPIC, MOZ_TOPIC, WNS_TOPIC}, groupId = "MessageHolderWithVariantsKafkaConsumer_group")
    public void listener(final MessageHolderWithVariants msg) {
        logger.info("Receiving messages from topic");
        dequeueEvent.fire(msg);
    }

}
