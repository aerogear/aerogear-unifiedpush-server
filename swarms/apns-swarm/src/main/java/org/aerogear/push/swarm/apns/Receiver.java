package org.aerogear.push.swarm.apns;

import org.aerogear.kafka.cdi.annotation.Consumer;
import org.aerogear.kafka.cdi.annotation.KafkaConfig;
import org.aerogear.push.swarm.apns.helper.MessageHolderWithTokens;
import org.aerogear.push.apns.NotificationSenderCallback;
import org.aerogear.push.apns.PushyApnsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@KafkaConfig(bootstrapServers = "#{KAFKA_SERVICE_HOST}:#{KAFKA_SERVICE_PORT}")
public class Receiver {

    private final Logger logger = LoggerFactory.getLogger(Receiver.class.getName());

    @Inject
    private PushyApnsSender sender;

    @Consumer(topics = "agpush_APNsTokenTopic", groupId = "swarm-ios")
    public void receiveMessage(final MessageHolderWithTokens messageContainer) {


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
