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

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class PubSubTest {
    private KafkaConsumer consumer;
    private KafkaProducer producer;
    private File dataDir;
    private KafkaCluster cluster;
    final private String topic = "testTopic";

    @Before
    public void beforeEach() throws IOException {
        dataDir = Testing.Files.createTestingDirectory("cluster");
        cluster = new KafkaCluster().usingDirectory(dataDir).withPorts(5000, 5001);

        cluster.addBrokers(1).startup();
        cluster.createTopic(topic, 1, 1);
    }

    @After
    public void afterEach() {
        close();
        cluster.shutdown();
        Testing.Files.delete(dataDir);
    }

    @Test
    public void testSendAndReceive() {
        producer = new KafkaProducer<>(producerProps());
        consumer = new KafkaConsumer<>(consumerProps());

        for(int i = 0; i < 10; i++) {
            producer.send(new ProducerRecord(topic, Integer.toString(i), "Message"));
        }

        consumer.subscribe(Arrays.asList(topic));
        ConsumerRecords<String, String> records = consumer.poll(200);

        for(ConsumerRecord<String, String> record : records) {
            assertEquals(record.value(), "Message");
        }
    }

    @Test
    public void producerNotNull() {
        producer = new KafkaProducer<>(producerProps());
        assertNotNull(producer);
    }

    @Test
    public void consumerNotNull() {
        consumer = new KafkaConsumer<>(consumerProps());
        assertNotNull(consumer);
    }

    private void close() {
        if(producer != null) {
            producer.close();
        }

        if(consumer != null) {
            consumer.close();
        }
    }

    private Properties producerProps() {
        Properties props = cluster.useTo().getProducerProperties(null);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    private Properties consumerProps() {
        Properties props = cluster.useTo().getConsumerProperties("10", null, OffsetResetStrategy.EARLIEST);
        props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

}