/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.kafka.consumers;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Kafka consumer wrapper class that allows its implementations to simply handle new incoming records.
 */
public abstract class AbstractKafkaConsumer implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(AbstractKafkaConsumer.class);

    /*
     * True if a consumer is running; otherwise false
     */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /*
     * Topics for which the consumer subscribes.
     */
    private List<String> topics;
    private KafkaConsumer<?, ?> consumer;

    public AbstractKafkaConsumer(String propertiesPath, List<String> topics) {
        Properties properties = null;
        try {
            properties = ConfigurationUtils.loadProperties(propertiesPath);
        } catch (IOException e) {
            logger.error("Consumer properties cannot be loaded.");
            e.printStackTrace();
        }
        consumer = new KafkaConsumer<>(properties);
        this.topics = topics;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(topics);
            logger.info("Consumer successfully subscribed for topics {}", topics);
            while (isRunning()) {
                ConsumerRecords<?, ?> records = consumer.poll(100);
                for (ConsumerRecord<?, ?> record : records) {
                    handleRecord(record);
                }
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (isRunning()) {
                throw e;
            }
        } finally {
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
        running.set(true);
        consumer.wakeup();
    }

    /*
     * Handle each consumed record.
     */
    public abstract void handleRecord(ConsumerRecord<?, ?> record);
}
