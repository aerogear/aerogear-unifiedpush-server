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
package org.jboss.aerogear.unifiedpush.kafka.producers;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.jboss.aerogear.unifiedpush.kafka.BasicKafkaTest;
import org.jboss.aerogear.unifiedpush.kafka.KafkaClusterConfig;
import org.jboss.aerogear.unifiedpush.kafka.utils.MockProviders;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.installations.InstallationRegistrationEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test cases for {@link InstallationRegistrationEndpoint#getInstallationMetricsProducer()} object.
 */
@RunWith(Arquillian.class)
public class InstallationMetricsProducerTest extends BasicKafkaTest {

    private KafkaConsumer<?, ?> consumer;

    @Inject
    InstallationRegistrationEndpoint installationRegistrationEndpoint;

    @Deployment
    public static JavaArchive createDeployment() {
        // deploy a jar
        return ShrinkWrap.create(JavaArchive.class)
                // add kafka configuration
                .addClass(KafkaClusterConfig.class)
                // add container for mock providers
                .addPackage(MockProviders.class.getPackage())
                // add class that uses the tested producer
                .addClass(InstallationRegistrationEndpoint.class).addClass(AbstractBaseEndpoint.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void produceTest() {

        String randomPushMessageId = UUID.randomUUID().toString();

        // send a record using the tested producer
        installationRegistrationEndpoint.getInstallationMetricsProducer().send(KafkaClusterConfig.KAFKA_INSTALLATION_TOPIC,
                randomPushMessageId);

        consumer = new KafkaConsumer<>(kafkaCluster.consumerPropperties());
        consumer.subscribe(Arrays.asList(KafkaClusterConfig.KAFKA_INSTALLATION_TOPIC));
        
        // wait 1 sec to consume the messages 
        ConsumerRecords<String, String> records = (ConsumerRecords<String, String>) consumer.poll(1000);

        // only one message is sent - so one has to be consumed
        assertEquals(1, records.count());
        for (ConsumerRecord<String, String> record : records) {
            // check if the consumed record is the same as the sent
            assertEquals(randomPushMessageId, record.value());
        }

        consumer.close();
    }
}
