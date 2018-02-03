package org.aerogear.push.swarm.fcm;

import org.aerogear.kafka.cdi.annotation.Consumer;
import org.aerogear.kafka.cdi.annotation.KafkaConfig;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.sender.FCMPushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@KafkaConfig(bootstrapServers = "172.17.0.4:9092")
public class FcmReceiver {


    private final Logger logger = LoggerFactory.getLogger(FcmReceiver.class.getName());

    @Inject
    private FCMPushNotificationSender sender;

    @Consumer(topics = "agpush_FCMTokenTopic", groupId = "swarm-fcm")
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