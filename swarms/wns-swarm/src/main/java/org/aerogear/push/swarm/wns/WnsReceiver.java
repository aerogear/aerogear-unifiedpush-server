package org.aerogear.push.swarm.wns;

import org.aerogear.kafka.cdi.annotation.Consumer;
import org.aerogear.kafka.cdi.annotation.KafkaConfig;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.WNSPushNotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@KafkaConfig(bootstrapServers = "#{KAFKA_SERVICE_HOST}:#{KAFKA_SERVICE_PORT}")
public class WnsReceiver {


    private final Logger logger = LoggerFactory.getLogger(WnsReceiver.class.getName());

    @Inject
    private WNSPushNotificationSender sender;

    @Consumer(topics = "agpush_WNSTokenTopic", groupId = "swarm-wns")
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