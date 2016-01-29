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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.AbstractJMSTest;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestTokenLoaderTransactionFailForGCM extends AbstractJMSTest {

    private static final int CONCURRENT_WORKERS = 15; // specified by maxSession MDB config in jboss-ejb.xml
    private static final int BATCH_SIZE = 1000; // maximum number of tokens in one batch for Android/GCM
    private static final int NUMBER_OF_BATCHES_TO_SEND = 120;
    private static final long TIME_TO_DELIVER_BATCH = 1000L; // in practice this depends on network delay/bandwidth between UPS instance and GCM servers
    private static final long MIN_TIME_TO_DELIVER_ALL_BATCHES = (NUMBER_OF_BATCHES_TO_SEND / CONCURRENT_WORKERS) * TIME_TO_DELIVER_BATCH;
    private static final long MAX_TIME_TO_DELIVER_ALL_BATCHES = MIN_TIME_TO_DELIVER_ALL_BATCHES + 2000L; // 2 sec tolerance

    @Inject @DispatchToQueue
    private Event<MessageHolderWithTokens> dispatchTokensEvent;

    private static String messageId;
    private static final CountDownLatch waitToDeliverAllBatches = new CountDownLatch(NUMBER_OF_BATCHES_TO_SEND);
    private static final AtomicInteger currentConcurrency = new AtomicInteger(0);
    private static final AtomicInteger maxConcurrency = new AtomicInteger(0);

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestTokenLoaderTransactionFailForGCM.class)
                .withMessaging()
                .withMessageDrivenBeans()
                .as(WebArchive.class);
    }

    @Test(timeout = MAX_TIME_TO_DELIVER_ALL_BATCHES)
    public void testAndroidTransactedRedelivery() throws InterruptedException {

        List<String> tokens = new ArrayList<String>();

        for (int i = 0; i < BATCH_SIZE; i++) {
            tokens.add("eHlfnI0__dI:APA91bEhtHefML2lr_sBQ-bdXIyEn5owzkZg_p_y7SRyNKRMZ3XuzZhBpTOYIh46tqRYQIc-7RTADk4nM5H-ONgPDWHodQDS24O5GuKP8EZEKwNh4Zxdv1wkZJh7cU2PoLz9gn4Nxqz-");
        }

        // given
        messageId = UUID.randomUUID().toString();
        PushMessageInformation pmi = new PushMessageInformation();
        pmi.setId(messageId);
        UnifiedPushMessage pushMessage = new UnifiedPushMessage();
        Variant variant = new AndroidVariant();
        variant.setType(VariantType.ANDROID);

        // when
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUMBER_OF_BATCHES_TO_SEND; i++) {
            while (true) {
                try {
                    MessageHolderWithTokens msg = new MessageHolderWithTokens(pmi, pushMessage, variant, tokens, i);
                    System.err.println(i);
                    dispatchTokensEvent.fire(msg);
                    break;
                } catch (Exception e) {
                    Thread.sleep(50);
                }
            }
        }

        // then
        long allBatchesLoaded = System.currentTimeMillis();
        waitToDeliverAllBatches.await();
        long finishedDeliveringOfAllBatchesOnceAllAreLoaded = System.currentTimeMillis() - allBatchesLoaded;
        long took = System.currentTimeMillis() - start;

        assertEquals("all workers are used to send notifications", CONCURRENT_WORKERS, maxConcurrency.get());
        if (took < MIN_TIME_TO_DELIVER_ALL_BATCHES) {
            fail(String.format("it should take at least %s ms to load and deliver all batches, but it took %s", MIN_TIME_TO_DELIVER_ALL_BATCHES, took));
        }
        if (finishedDeliveringOfAllBatchesOnceAllAreLoaded < TIME_TO_DELIVER_BATCH) {
            fail(String.format("it should take at least %s ms to deliver all batches once all tokens were loaded, but it took %s", TIME_TO_DELIVER_BATCH, finishedDeliveringOfAllBatchesOnceAllAreLoaded));
        }
        if (finishedDeliveringOfAllBatchesOnceAllAreLoaded > TIME_TO_DELIVER_BATCH * 2) {
            fail(String.format("it should take at most %s ms to deliver all batches once all tokens were loaded, but it took %s", TIME_TO_DELIVER_BATCH * 2, finishedDeliveringOfAllBatchesOnceAllAreLoaded));
        }
    }

    public void observeMessage(@Observes @Dequeue MessageHolderWithTokens msg) throws InterruptedException {
        if (msg.getPushMessageInformation().getId().equals(messageId)) {
            currentConcurrency.incrementAndGet();
            maxConcurrency.set(Math.max(currentConcurrency.get(), maxConcurrency.get()));
            System.err.println("started processing: " + msg.getSerialId());
            Thread.sleep(TIME_TO_DELIVER_BATCH);
            System.err.println("finished processing: " + msg.getSerialId());
            currentConcurrency.decrementAndGet();
            waitToDeliverAllBatches.countDown();
        }
    }
}
