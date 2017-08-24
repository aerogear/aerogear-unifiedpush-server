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

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import org.jboss.aerogear.unifiedpush.kafka.utils.MockProviders;
import org.jboss.aerogear.unifiedpush.kafka.BaseKafkaTest;
import org.jboss.aerogear.unifiedpush.kafka.KafkaClusterConfig;
import org.jboss.aerogear.unifiedpush.kafka.MessageConsumedEvent;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.installations.InstallationRegistrationEndpoint;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import net.wessendorf.kafka.impl.DelegationKafkaConsumer;

/**
 * Test cases for {@link InstallationMetricsConsumer#consume(String)} method.
 */
@RunWith(Arquillian.class)
public class InstallationMetricsConsumerTest extends BaseKafkaTest {

    private boolean isMethodCalled = Boolean.FALSE;
    private static CountDownLatch countDownLatch;

    @Inject
    InstallationRegistrationEndpoint installationRegistrationEndpoint;

    @Deployment
    public static JavaArchive createDeployment() {

        // deploy a jar file
        return ShrinkWrap.create(JavaArchive.class)
                // add Kafka configurations
                .addClass(KafkaClusterConfig.class)
                // add the consumer that is tested
                .addClass(InstallationMetricsKafkaConsumer.class)
                // add a container for all mock providers
                .addPackage(MockProviders.class.getPackage())
                // needed to the cdi library
                .addClass(DelegationKafkaConsumer.class).addClass(InstallationRegistrationEndpoint.class)
                .addClass(AbstractBaseEndpoint.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void consumeTest() throws InterruptedException {
        countDownLatch = new CountDownLatch(1);

        String randomAerogearPushId = UUID.randomUUID().toString();

        // test if when consumer receives a message, updateAnalytics method is invoked
        PushMessageMetricsService pushMessageServiceMock = MockProviders.getPushMessageMetricsService();
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                isMethodCalled = Boolean.TRUE;
                return null;
            }
        }).when(pushMessageServiceMock).updateAnalytics(randomAerogearPushId);

        // wait so the consumer is properly initialized
        Thread.sleep(5000);

        // use a producer to send a message to tested consumer's topic
        installationRegistrationEndpoint.getInstallationMetricsProducer().send(KafkaClusterConfig.KAFKA_INSTALLATION_TOPIC,
                randomAerogearPushId);

        // wait until the message is consumed
        countDownLatch.await();
        Assert.assertTrue(isMethodCalled);
        
    }

    /*
     * Observes when a consumer consumes a message.
     */
    public void observeMessageConsumed(@Observes MessageConsumedEvent msg) {
        countDownLatch.countDown();
    }
}
