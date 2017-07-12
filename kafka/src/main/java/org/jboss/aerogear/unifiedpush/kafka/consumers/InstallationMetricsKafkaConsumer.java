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

/**
 * Kafka Consumer that reads from "installationMetrics" topic a pair (PushMessageID, VariantID) and updates analytics by
 * invocation of {@link PushMessageMetricsService#updateAnalytics(String, String)}.
 * 
 */
public class InstallationMetricsKafkaConsumer {

    private final Logger logger = LoggerFactory.getLogger(InstallationMetricsKafkaConsumer.class);

    /**
     * Consumer's topic.
     */
    public static final String KAFKA_INSTALLATION_TOPIC = "installationMetrics";

    /**
     * Consumer's groupId.
     */
    public static final String KAFKA_INSTALLATION_TOPIC_CONSUMER_GROUP_ID = "installationMetricsGroup";

    @Inject
    private PushMessageMetricsService metricsService;

    /**
     * A method invoked for each record that a consumer reads. It updates metrics analytics based on push message id and variant
     * id.
     */
    @Consumer(topic = KAFKA_INSTALLATION_TOPIC, groupId = KAFKA_INSTALLATION_TOPIC_CONSUMER_GROUP_ID)
    public void receiver(final String pushMessageId, final String variantId) {
        logger.info("Update metric analytics for push message's ID {} and variant's ID {}", pushMessageId, variantId);
        metricsService.updateAnalytics(pushMessageId, variantId);
    }
}
