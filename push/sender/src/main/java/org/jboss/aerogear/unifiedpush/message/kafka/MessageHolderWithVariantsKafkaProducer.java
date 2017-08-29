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

import net.wessendorf.kafka.SimpleKafkaProducer;
import net.wessendorf.kafka.cdi.annotation.Producer;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;

public class MessageHolderWithVariantsKafkaProducer {

    private final Logger logger = LoggerFactory.getLogger(MessageHolderWithVariantsKafkaProducer.class);

    @Producer
    private SimpleKafkaProducer<String, MessageHolderWithVariants> producer;

    private final String ADM_TOPIC = "agpush_AdmMessageTopic";

    private final String APNS_TOPIC = "agpush_APNsMessageTopic";

    private final String FCM_TOPIC = "agpush_FCMMessageTopic";

    private final String MPNS_TOPIC = "agpush_MPNSMessageTopic";

    private final String MOZ_TOPIC = "agpush_SimplePushMessageTopic";

    private final String WNS_TOPIC = "agpush_WNSMessageTopic";

    public void queueMessageVariantForProcessing(@Observes @DispatchToQueue MessageHolderWithVariants msg) {
        final String pushTopic = selectTopic(msg.getVariantType());
        logger.info("Sending message to the {} topic", pushTopic);

        producer.send(pushTopic, msg);
    }

    private String selectTopic(final VariantType variantType) {
        switch (variantType) {
            case ADM:
                return ADM_TOPIC;
            case ANDROID:
                return FCM_TOPIC;
            case IOS:
                return APNS_TOPIC;
            case SIMPLE_PUSH:
                return MOZ_TOPIC;
            case WINDOWS_MPNS:
                return MPNS_TOPIC;
            case WINDOWS_WNS:
                return WNS_TOPIC;
            default:
                throw new IllegalStateException("Unknown variant type queue");
        }
    }

}
