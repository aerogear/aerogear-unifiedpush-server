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

    private final String ADM_TOPIC = "AdmMessage_Topic";

    private final String APNS_TOPIC = "APNsMessage_Topic";

    private final String FCM_TOPIC = "FCMMessage_Topic";

    private final String MPNS_TOPIC = "MPNSMessage_Topic";

    private final String MOZ_TOPIC = "SimplePushMessage_Topic";

    private final String WNS_TOPIC = "WNSMessage_Topic";

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
