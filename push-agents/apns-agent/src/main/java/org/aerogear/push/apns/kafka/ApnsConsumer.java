package org.aerogear.push.apns.kafka;

import org.aerogear.push.apns.helper.MessageHolderWithTokens;
import org.aerogear.push.apns.ios.NotificationSenderCallback;
import org.aerogear.push.apns.ios.PushyApnsSender;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.aerogear.push.apns.helper.Resolver.resolve;

public class ApnsConsumer implements Runnable {

    private static final String KAFKA_SERVICE_HOST = "KAFKA_SERVICE_HOST";
    private static final String KAFKA_SERVICE_PORT = "KAFKA_SERVICE_PORT";
    private static final String APNS_TOPIC = "agpush_APNsTokenTopic";
    private static final String KAFKA_APNS_CONSUMER_GROUP = "apns-agents-ocp";

    private final AtomicBoolean running = new AtomicBoolean(Boolean.TRUE);
    private static final Logger LOGGER = Logger.getLogger(ApnsConsumer.class.getName());
    private final KafkaConsumer<String, MessageHolderWithTokens> consumer;
    private final PushyApnsSender sender;


    public ApnsConsumer() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, resolveKafkaService());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, KAFKA_APNS_CONSUMER_GROUP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GenericDeserializer.class);
        consumer = new KafkaConsumer(props);
        sender = new PushyApnsSender();
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(Collections.singleton(APNS_TOPIC));
            while (isRunning()) {
                final ConsumerRecords<String, MessageHolderWithTokens> records = consumer.poll(Long.MAX_VALUE);

                records.forEach(record -> {

                    final MessageHolderWithTokens messageContainer = record.value();
                    sender.sendPushMessage(messageContainer.getVariant(), messageContainer.getDeviceTokens(), messageContainer.getUnifiedPushMessage(), messageContainer.getPushMessageInformation().getId(), new NotificationSenderCallback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(String reason) {

                        }
                    });
                });
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (isRunning()) {
                LOGGER.log(Level.FINE, "Exception on close", e);
                throw e;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "error", e);

        } finally {
            LOGGER.info("Close the consumer.");
            consumer.close();
        }
    }

    /**
     * True when a consumer is running; otherwise false
     */
    public boolean isRunning() {
        return running.get();
    }

    /*
     * Shutdown hook which can be called from a separate thread.
     */
    public void shutdown() {
        LOGGER.info("Shutting down the consumer.");
        running.set(Boolean.FALSE);
        consumer.wakeup();
    }

    private static String resolveKafkaService() {

        return new StringBuilder()
                .append(resolve(KAFKA_SERVICE_HOST))
                .append(":")
                .append(resolve(KAFKA_SERVICE_PORT)).toString();
    }

}
