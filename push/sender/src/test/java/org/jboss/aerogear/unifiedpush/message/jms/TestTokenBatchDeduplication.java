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
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.message.SenderConfig;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.service.AbstractNoCassandraServiceTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import reactor.core.publisher.WorkQueueProcessor;

// we will be able to receive a message just once
// any other try for sending the message with same ID...
// ...will again mean the message won't be accepted to the queue (will be de-duplicated based on its ID)
// but sending message with different ID will again deliver message
// it doesn't matter how many times we send the message, ...
/**
 * JBoss, Home of Professional Open Source Copyright Red Hat, Inc., and
 * individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
@ContextConfiguration(classes = { SenderConfig.class })
public class TestTokenBatchDeduplication extends AbstractNoCassandraServiceTest {
	private static final long TEST_TIMEOUT = 5000;

	@Inject
	private WorkQueueProcessor<MessageHolderWithTokens> dispatchTokenBatch;
	private static AtomicInteger counter;

	private static String uuid = UUID.randomUUID().toString();

	@Before
	public void setUp() {
		counter = new AtomicInteger(0);
		// Recreate WorkQueueProcessor for next test
		dispatchTokenBatch = WorkQueueProcessor.<MessageHolderWithTokens>builder().build();
	}

	@Test(timeout = TEST_TIMEOUT)
	public void given_the_message_is_sent_twice_then_it_is_deduplicated_and_received_exactly_once()
			throws InterruptedException {
		sendBatchWithSerialId(1);
		sendBatchWithSerialId(1);

		dispatchTokenBatch.take(2).distinct(m -> m.getSerialId()).subscribe(s -> receiveMessage(s));

		// we will be able to receive a message just once
		Thread.sleep(100);
		assertEquals(1, counter.get());
	}

	@Test(timeout = TEST_TIMEOUT)
	public void given_the_message_was_already_sent_then_sending_it_again_will_deduplicate_it()
			throws InterruptedException {

		dispatchTokenBatch.take(2).distinct(m -> m.getSerialId()).subscribe(s -> receiveMessage(s));

		// any other try for sending the message with same ID...
		sendBatchWithSerialId(1);

		// ...will again mean the message won't be accepted to the queue (will
		// be de-duplicated based on its ID)
		Thread.sleep(100);
		assertEquals(1, counter.get());
	}

	@Test(timeout = TEST_TIMEOUT)
	public void given_the_message_has_different_serialId_then_it_can_be_again_delivered_exactly_once()
			throws InterruptedException {

		dispatchTokenBatch.take(2).distinct().subscribe(s -> receiveMessage(s));

		// but sending message with different ID will again deliver message
		sendBatchWithSerialId(2);
		sendBatchWithSerialId(2);

		Thread.sleep(200);
		assertEquals(2, counter.get());
	}

	private void sendBatchWithSerialId(int serialId) {
		List<String> tokenBatch = new ArrayList<>();
		FlatPushMessageInformation pmi = new FlatPushMessageInformation();
		pmi.setId(uuid);
		AndroidVariant variant = new AndroidVariant();
		MessageHolderWithTokens msg = new MessageHolderWithTokens(pmi, null, variant, tokenBatch, serialId);

		// it doesn't matter how many times we send the message, ...
		dispatchTokenBatch.onNext(msg);
	}

	public void receiveMessage(MessageHolderWithTokens msg) {
		counter.incrementAndGet();
	}
}