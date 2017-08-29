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

import java.io.IOException;

import org.jboss.aerogear.unifiedpush.kafka.consumers.InstallationMetricsConsumerTest;
import org.junit.After;
import org.junit.Before;

/**
 * Basic class that starts up a Kafka cluster before each test case and stops it afterwards. Each test class that uses embedded
 * Kafka cluster has to extend this. See usage {@link InstallationMetricsConsumerTest}.
 */
public class BaseKafkaTest {

    protected KafkaClusterWrapper kafkaCluster = new KafkaClusterWrapper();
    /**
     * Starts up Kafka cluster before each test case.
     */
    @Before
    public void beforeEach() throws IOException, InterruptedException {
        kafkaCluster.start();
    }

    /**
     * Stops Kafka cluster after each test case.
     */
    @After
    public void afterEach() {
        kafkaCluster.stop();
    }

}
