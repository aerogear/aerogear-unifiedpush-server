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

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.AbstractJMSTest;
import org.jboss.aerogear.unifiedpush.message.event.TriggerMetricCollection;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestTriggerMetricCollectionDeduplication extends AbstractJMSTest {

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestTriggerMetricCollectionDeduplication.class)
                .withMessaging()
                .withMessageDrivenBeans()
                .addClasses(CdiJmsBridge.class)
                .as(WebArchive.class);
    }

    @Inject @DispatchToQueue
    private Event<TriggerMetricCollection> triggerMetricCollection;

    @Resource(mappedName = "java:/queue/TriggerMetricCollectionQueue")
    private Queue triggerMetricCollectionQueue;

    private static final ConcurrentLinkedQueue<TriggerMetricCollection> receivedTriggers = new ConcurrentLinkedQueue<TriggerMetricCollection>();
    private static final CountDownLatch firstMessageLatch = new CountDownLatch(1);
    private static String messageId;

    @Test(timeout = 5000)
    public void testDeduplication() throws InterruptedException {
        messageId = UUID.randomUUID().toString();

        TriggerMetricCollection msg = new TriggerMetricCollection(messageId);

        // it doesn't matter how many times we send the message, ...
        triggerMetricCollection.fire(msg);
        triggerMetricCollection.fire(msg);

        firstMessageLatch.await(2500L, TimeUnit.MILLISECONDS);
        // we will be able to receive it just once
        assertEquals("first try for receiving the message should receive it successfully", 1, receivedTriggers.size());
        // and any other try to receive it again won't succeed
        // since it will be dropped by the queue (that is detecting duplicates by ID)
        Thread.sleep(1000L);
        assertEquals("but second try should not receive any further message since it was de-duplicated", 1, receivedTriggers.size());

        // any other try for sending the message with same ID (even though we are sending different object in terms of equality)
        msg = new TriggerMetricCollection(messageId);
        triggerMetricCollection.fire(msg);
        triggerMetricCollection.fire(msg);

        // ...will again mean the message will be rejected by the queue
        Thread.sleep(1000L);
        assertEquals("any other try should not receive any further message since it was de-duplicated", 1, receivedTriggers.size());
    }

    public void receiveTrigger(@Observes @Dequeue TriggerMetricCollection triggerEvent) {
        if (triggerEvent.getPushMessageInformationId().equals(messageId)) {
            triggerEvent.markAllVariantsProcessed(); // mark processed, otherwise it will be rolled-back and redelivered
            receivedTriggers.add(triggerEvent);
            if (receivedTriggers.size() == 1) {
                firstMessageLatch.countDown();
            }
        }
    }
}
