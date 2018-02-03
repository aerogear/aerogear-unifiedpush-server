package org.aerogear.push.iosserver.rest;

import org.aerogear.kafka.cdi.annotation.Consumer;
import org.aerogear.kafka.cdi.annotation.KafkaConfig;
import org.aerogear.push.iosserver.rest.helper.MessageHolderWithTokens;
import org.aerogear.push.iosserver.rest.org.aerogear.push.sender.ios.NotificationSenderCallback;
import org.aerogear.push.iosserver.rest.org.aerogear.push.sender.ios.PushyApnsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@KafkaConfig(bootstrapServers = "172.17.0.4:9092")
public class Receiver {

    private final Logger logger = LoggerFactory.getLogger(Receiver.class.getName());

    @Inject
    private PushyApnsSender sender;

    @Consumer(topics = "agpush_APNsTokenTopic", groupId = "swarm-ios")
    public void receiveMessage(final MessageHolderWithTokens messageContainer) {

        logger.error("DA -> " + messageContainer);
        logger.error("DA -> " + sender);



        sender.sendPushMessage(messageContainer.getVariant(), messageContainer.getDeviceTokens(), messageContainer.getUnifiedPushMessage(), messageContainer.getPushMessageInformation().getId(), new NotificationSenderCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(String reason) {

            }
        });

    }

}
