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
package org.jboss.aerogear.unifiedpush.kafka.consumers;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wessendorf.kafka.cdi.annotation.Consumer;

import static org.jboss.aerogear.unifiedpush.kafka.KafkaClusterConfig.KAFKA_INSTALLATION_TOPIC;
import static org.jboss.aerogear.unifiedpush.kafka.KafkaClusterConfig.KAFKA_INSTALLATION_TOPIC_CONSUMER_GROUP_ID;

/**
 * Kafka Consumer that reads the VariantID from "agpush_installationMetrics" and updates analytics by
 * invocation of {@link PushMessageMetricsService#updateAnalytics(String)}.
 * 
 */
public class InstallationMetricsKafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(InstallationMetricsKafkaConsumer.class);

    @Inject
    private PushMessageMetricsService metricsService;

    /**
     * Update metrics analytics based on push message id from the consumed record.
     */
    @Consumer(topic = KAFKA_INSTALLATION_TOPIC, groupId = KAFKA_INSTALLATION_TOPIC_CONSUMER_GROUP_ID)
    public void receiver(final String pushMessageId) {
        logger.info("Update metric analytics for push message's ID {}", pushMessageId);
        metricsService.updateAnalytics(pushMessageId);
    }
}
