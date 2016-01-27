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
package org.jboss.aerogear.unifiedpush.message.jms;

import static org.junit.Assert.assertTrue;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.message.AbstractJMSTest;
import org.jboss.aerogear.unifiedpush.message.event.TriggerMetricCollection;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestTriggerMetricCollectionRedelivery extends AbstractJMSTest {

    private static final int NUMBER_OF_FAILURES_UNTIL_SUCCESS = 5;
    private static final long DELIVERY_DELAY_TOLERANCE_MS = 100L;

    @Inject @DispatchToQueue
    private Event<TriggerMetricCollection> triggerMetricCollection;

    private static String messageId;
    private static final CountDownLatch latch = new CountDownLatch(NUMBER_OF_FAILURES_UNTIL_SUCCESS);
    private static Long lastDelivery;

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestTriggerMetricCollectionRedelivery.class)
                .withMessaging()
                    .addClasses(MessageHolderWithTokensProducer.class, MessageHolderWithTokensConsumer.class, AbstractJMSMessageListener.class)
                    .addAsWebInfResource("jboss-ejb3-message-holder-with-tokens.xml", "jboss-ejb3.xml")
                .addClasses(CdiJmsBridge.class)
                .addClasses(TriggerMetricCollectionConsumer.class)
                .as(WebArchive.class);
    }

    @Test(timeout = 6000)
    public void testTransactedRedelivery() throws InterruptedException {
        // given
        messageId = UUID.randomUUID().toString();
        TriggerMetricCollection msg = new TriggerMetricCollection(messageId);

        // when
        triggerMetricCollection.fire(msg);

        // then
        latch.await();
    }

    public void observeMessage(@Observes @Dequeue TriggerMetricCollection msg) {
        if (msg.getPushMessageInformationId().equals(messageId)) {
            if (lastDelivery != null) {
                long deltaMs = Math.abs(System.currentTimeMillis() - lastDelivery - TriggerMetricCollection.REDELIVERY_DELAY_MS);
                assertTrue("redelivery must be performed timely with less than " + DELIVERY_DELAY_TOLERANCE_MS + "ms tolerance, but was " + deltaMs,
                        deltaMs < DELIVERY_DELAY_TOLERANCE_MS);
            }
            if (latch.getCount() == 0) {
                msg.markAllVariantsProcessed();
            } else {
                lastDelivery = System.currentTimeMillis();
                System.out.println("redelivery attempt #" + (NUMBER_OF_FAILURES_UNTIL_SUCCESS - latch.getCount()));
            }
            latch.countDown();
        }
    }
}
