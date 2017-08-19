package org.jboss.aerogear.unifiedpush.message.kafka;

import net.wessendorf.kafka.cdi.annotation.Consumer;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * Created by matzew on 8/16/17.
 */
public class MessageHolderWithVariantsKafkaConsumer {


    private final Logger logger = LoggerFactory.getLogger(MessageHolderWithVariantsKafkaConsumer.class);

    private final String ADM_TOPIC = "AdmMessage_Topic";

    private final String APNS_TOPIC = "APNsMessage_Topic";

    private final String FCM_TOPIC = "FCMMessage_Topic";

    private final String MPNS_TOPIC = "MPNSMessage_Topic";

    private final String MOZ_TOPIC = "SimplePushMessage_Topic";

    private final String WNS_TOPIC = "WNSMessage_Topic";


    @Inject
    @Dequeue
    private Event<MessageHolderWithVariants> dequeueEvent;

    @Consumer(topics = {ADM_TOPIC, APNS_TOPIC, FCM_TOPIC, MPNS_TOPIC, MOZ_TOPIC, WNS_TOPIC}, groupId = "MessageHolderWithVariantsKafkaConsumer_group")
    public void listener(final MessageHolderWithVariants msg) {

        logger.info("receiving messages from topic");
        dequeueEvent.fire(msg);
    }


}
