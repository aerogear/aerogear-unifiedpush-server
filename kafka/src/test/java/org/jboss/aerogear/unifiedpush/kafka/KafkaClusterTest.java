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
package org.jboss.aerogear.unifiedpush.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.junit.After;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test cases for plain Kafka producer and consumer.
 */
public class KafkaClusterTest extends BaseKafkaTest {

    private KafkaConsumer consumer;
    private KafkaProducer producer;
    final private String topic = "testTopic";

    @After
    public void afterEach() {
        close();
        super.afterEach();
    }

    @Test
    public void testSendAndReceive() {
        producer = new KafkaProducer<>(kafkaCluster.producerProperties());
        consumer = new KafkaConsumer<>(kafkaCluster.consumerPropperties());

        for (int i = 0; i < 10; i++) {
            producer.send(new ProducerRecord(topic, Integer.toString(i), "Message"));
        }

        consumer.subscribe(Arrays.asList(topic));
        ConsumerRecords<String, String> records = consumer.poll(200);

        for (ConsumerRecord<String, String> record : records) {
            assertEquals(record.value(), "Message");
        }
    }

    @Test
    public void producerNotNull() {
        producer = new KafkaProducer<>(kafkaCluster.producerProperties());
        assertNotNull(producer);
    }

    @Test
    public void consumerNotNull() {
        consumer = new KafkaConsumer<>(kafkaCluster.consumerPropperties());
        assertNotNull(consumer);
    }

    /**
     * Close created producers/consumers.
     */
    private void close() {
        if (producer != null) {
            producer.close();
        }

        if (consumer != null) {
            consumer.close();
        }
    }
}