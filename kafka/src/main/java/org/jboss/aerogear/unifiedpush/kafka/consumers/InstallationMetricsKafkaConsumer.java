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

import java.util.Arrays;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Kafka Consumer that reads from "installationMetrics" topic a pair (PushMessageID, VariantID) and updates analytics by
 * invocation of {@link PushMessageMetricsService#updateAnalytics(String, String)}.
 * 
 */
public class InstallationMetricsKafkaConsumer extends AbstractKafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(InstallationMetricsKafkaConsumer.class);

    /**
     * Consumer's topic.
     */
    public static final String KAFKA_INSTALLATION_TOPIC = "installationMetrics";

    /**
     * Path to consumer's properties file.
     */
    public static final String KAFKA_CONSUMER_PROPERTIES_PATH = "/kafka/consumer.properties";

    /**
     * Service that updates metrics analytics.
     */
    private PushMessageMetricsService metricsService;

    public InstallationMetricsKafkaConsumer(PushMessageMetricsService metricsService) {
        super(KAFKA_CONSUMER_PROPERTIES_PATH, Arrays.asList(KAFKA_INSTALLATION_TOPIC));
        this.metricsService = metricsService;
    }

    @Override
    public void handleRecord(ConsumerRecord<?, ?> record) {
        logger.info("Update metric analytics for push message's ID {} and variant's ID {}", record.key(), record.value());
        metricsService.updateAnalytics((String) record.key(), (String) record.value());
    }
}
