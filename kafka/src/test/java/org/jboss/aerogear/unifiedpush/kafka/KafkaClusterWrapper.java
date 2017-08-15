/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.kafka;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;

/**
 * Kafka cluster wrapper class that adds useful methods for using it in the test environment.
 */
public class KafkaClusterWrapper {

    public static final String KAFKA_HOST = "localhost";
    public static final Integer KAFKA_PORT = 5001;
    
    private File dataDir;
    private KafkaCluster cluster;

    public void start() throws IOException {
        dataDir = Testing.Files.createTestingDirectory("cluster");
        cluster = new KafkaCluster().usingDirectory(dataDir)
                .withPorts(5000, KAFKA_PORT);
        cluster.addBrokers(1).startup();
    }

    public Properties producerProperties() {
        Properties props = cluster.useTo().getProducerProperties(null);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    public Properties consumerPropperties() {
        Properties props = cluster.useTo().getConsumerProperties("10", null, OffsetResetStrategy.EARLIEST);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

    public void stop() {
        cluster.shutdown();
        Testing.Files.delete(dataDir);
    }
}
