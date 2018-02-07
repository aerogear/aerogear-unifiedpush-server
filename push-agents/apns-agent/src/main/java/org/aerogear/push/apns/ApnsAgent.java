package org.aerogear.push.apns;

import org.aerogear.push.apns.kafka.ApnsConsumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApnsAgent {

    private static final Logger LOGGER = Logger.getLogger(ApnsAgent.class.getName());

    public static void main(String... args) {

        LOGGER.info("Starting Kafka Consumers");
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final ApnsConsumer apnsConsumer = new ApnsConsumer();
        executor.submit(apnsConsumer);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            apnsConsumer.shutdown();
            executor.shutdown();
            try {
                executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ie) {
                LOGGER.log(Level.SEVERE, "Error on close", ie);
            }
        }));
   }

}