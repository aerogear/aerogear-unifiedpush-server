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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.message.AbstractJMSTest;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.util.QueueUtils;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestTokenBatchDeduplication extends AbstractJMSTest {

    private static final long TEST_TIMEOUT = 5000;

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestTokenBatchDeduplication.class)
                .withMessaging()
                .withMessageDrivenBeans()
                .addClass(QueueUtils.class)
                .as(WebArchive.class);
    }

    @Inject @DispatchToQueue
    private Event<MessageHolderWithTokens> dispatchTokenBatch;

    @Resource(mappedName = "java:/queue/GCMTokenBatchQueue")
    private Queue androidTokenBatchQueue;

    private static CountDownLatch firstBatch = new CountDownLatch(1);
    private static CountDownLatch secondBatch = new CountDownLatch(2);
    private static AtomicInteger counter = new AtomicInteger(0);

    private static String uuid = UUID.randomUUID().toString();

    @InSequence(1)
    @Test(timeout = TEST_TIMEOUT)
    public void given_the_message_is_sent_twice_then_it_is_deduplicated_and_received_exactly_once() throws InterruptedException, JMSException {
        sendBatchWithSerialId(1);
        sendBatchWithSerialId(1);

        // we will be able to receive a message just once
        firstBatch.await();
        Thread.sleep(2500);
        assertEquals(1, counter.get());
    }

    @InSequence(2)
    @Test(timeout = TEST_TIMEOUT)
    public void given_the_message_was_already_sent_then_sending_it_again_will_deduplicate_it() throws InterruptedException, JMSException {
        // any other try for sending the message with same ID...
        sendBatchWithSerialId(1);
        // ...will again mean the message won't be accepted to the queue (will be de-duplicated based on its ID)
        Thread.sleep(2500);
        assertEquals(1, counter.get());
    }

    @InSequence(3)
    @Test(timeout = TEST_TIMEOUT)
    public void given_the_message_has_different_serialId_then_it_can_be_again_delivered_exactly_once() throws InterruptedException, JMSException {
        // but sending message with different ID will again deliver message
        sendBatchWithSerialId(2);
        sendBatchWithSerialId(2);
        secondBatch.await();
        Thread.sleep(2500);
        assertEquals(2, counter.get());
    }

    private void sendBatchWithSerialId(int serialId) {
        List<String> tokenBatch = new ArrayList<>();
        FlatPushMessageInformation pmi = new FlatPushMessageInformation();
        pmi.setId(uuid);
        AndroidVariant variant = new AndroidVariant();
        MessageHolderWithTokens msg = new MessageHolderWithTokens(pmi, null, variant, tokenBatch, serialId);

        // it doesn't matter how many times we send the message, ...
        dispatchTokenBatch.fire(msg);
    }

    public void receiveMessage(@Observes @Dequeue MessageHolderWithTokens msg) {
        counter.incrementAndGet();
        firstBatch.countDown();
        secondBatch.countDown();
    }
}
