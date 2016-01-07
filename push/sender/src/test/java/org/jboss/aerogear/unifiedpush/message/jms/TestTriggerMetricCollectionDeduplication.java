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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import javax.annotation.Resource;
import javax.enterprise.event.Event;
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
                    .addClasses(MessageHolderWithTokensProducer.class, MessageHolderWithTokensConsumer.class, AbstractJMSMessageListener.class)
                    .addAsWebInfResource("jboss-ejb3-message-holder-with-tokens.xml", "jboss-ejb3.xml")
                .addClasses(CdiJmsBridge.class)
                .as(WebArchive.class);
    }

    @Inject @DispatchToQueue
    private Event<TriggerMetricCollection> triggerMetricCollection;

    @Resource(mappedName = "java:/queue/TriggerMetricCollectionQueue")
    private Queue triggerMetricCollectionQueue;

    @Test(timeout = 5000)
    public void testDeduplication() throws InterruptedException {
        String uuid = UUID.randomUUID().toString();

        TriggerMetricCollection msg = new TriggerMetricCollection(uuid);

        final String msgSelector = String.format("_HQ_DUPL_ID = '%s'", uuid);

        // it doesn't matter how many times we send the message, ...
        triggerMetricCollection.fire(msg);
        triggerMetricCollection.fire(msg);

        // we will be able to receive it just once
        assertNotNull("first try for receiving the message should receive it successfully", receive().withTimeout(2500).withSelector(msgSelector).from(triggerMetricCollectionQueue));
        // and any other try to receive it again won't succeed
        // since it will be dropped by the queue (that is detecting duplicates by ID)
        assertNull("but second try should not receive any message since it was de-duplicated", receive().withTimeout(1000).withSelector(msgSelector).from(triggerMetricCollectionQueue));

        // any other try for sending the message with same ID (even though we are sending different object in terms of equality)
        msg = new TriggerMetricCollection(uuid);
        triggerMetricCollection.fire(msg);
        triggerMetricCollection.fire(msg);

        // ...will again mean the message will be rejected by the queue
        assertNull("any other try should not receive any message since it was de-duplicated", receive().withTimeout(1000).withSelector(msgSelector).from(triggerMetricCollectionQueue));
    }
}
