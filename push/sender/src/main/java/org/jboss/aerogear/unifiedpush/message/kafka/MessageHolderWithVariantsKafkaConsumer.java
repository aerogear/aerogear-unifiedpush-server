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

/**
 * Consumes {@link MessageHolderWithVariants} objects from various topics
 * and triggers {@link org.jboss.aerogear.unifiedpush.message.token.TokenLoader} for further
 * processing
 */
public class MessageHolderWithVariantsKafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(MessageHolderWithVariantsKafkaConsumer.class);

    private final String ADM_TOPIC = "agpush_admPushMessageTopic";

    private final String ANDROID_TOPIC = "agpush_gcmPushMessageTopic";

    private final String IOS_TOPIC = "agpush_apnsPushMessageTopic";

    private final String SIMPLE_PUSH_TOPIC = "agpush_simplePushMessageTopic";

    private final String WINDOWS_MPNS_TOPIC = "agpush_mpnsPushMessageTopic";

    private final String WINDOWS_WNS_TOPIC = "agpush_wnsPushMessageTopic";

    @Inject
    @Dequeue
    private Event<MessageHolderWithVariants> dequeueEvent;

    @Consumer(topics = {ADM_TOPIC, ANDROID_TOPIC, IOS_TOPIC, SIMPLE_PUSH_TOPIC, WINDOWS_MPNS_TOPIC, WINDOWS_WNS_TOPIC}, groupId = "agpush_messageHolderWithVariantsKafkaConsumerGroup")
    public void listener(final MessageHolderWithVariants msg) {
        logger.info("Receiving messages from topic");
        dequeueEvent.fire(msg);
    }

}
