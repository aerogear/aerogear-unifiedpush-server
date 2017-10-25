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

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.service.AbstractNoCassandraServiceTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.WorkQueueProcessor;

public class TestMessageRedelivery extends AbstractNoCassandraServiceTest {
	private static final Logger logger = LoggerFactory.getLogger(TestMessageRedelivery.class);

	private static final int NUMBER_OF_MESSAGES = 10000;
	private static final int DEFAULT_QUEUE_SIZE = 256;
	private static final int DEFAULT_WAIT = 3;

	private UnifiedPushMessage message;
	private FlatPushMessageInformation information;
	private Variant variant;
	private Collection<String> deviceTokens;

	private static CountDownLatch delivered;
	private static CountDownLatch failed;

	private WorkQueueProcessor<MessageHolderWithTokens> eventProcessor;

	@Before
	public void setUp() {
		information = new FlatPushMessageInformation();
		message = new UnifiedPushMessage();
		deviceTokens = new ArrayList<>();

		// Recreate WorkQueueProcessor for next test
		eventProcessor = WorkQueueProcessor.<MessageHolderWithTokens>builder().build();
	}

	@Test
	public void testMessageWillBeRedelivered() throws InterruptedException {
		// given
		variant = new AndroidVariant();
		delivered = new CountDownLatch(NUMBER_OF_MESSAGES);

		// Simulate taking first message only every time
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			eventProcessor.take(1).repeat(NUMBER_OF_MESSAGES).subscribe(s -> emulateMessageProcessingForRedelivery(s));
		}

		// when
		for (int i = 1; i <= NUMBER_OF_MESSAGES; i++) {
			eventProcessor.onNext(new MessageHolderWithTokens(information, message, variant, deviceTokens, i));
		}

		// then
		if (!delivered.await(1, TimeUnit.SECONDS)) {
			fail(String.format("all messages must be delivered (remains %s)", delivered.getCount()));
		}
	}

	@Test
	public void testMessageCountMultipleSubscribers() throws InterruptedException {
		// given
		variant = new SimplePushVariant();
		failed = new CountDownLatch(NUMBER_OF_MESSAGES);

		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			eventProcessor.repeat(NUMBER_OF_MESSAGES).subscribe(s -> emulateRedeliverableMessageProcessing(s));
		}

		// when
		for (int i = 1; i <= NUMBER_OF_MESSAGES; i++) {
			eventProcessor.onNext(new MessageHolderWithTokens(information, message, variant, deviceTokens, i));
		}

		// then
		if (!failed.await(DEFAULT_WAIT, TimeUnit.SECONDS)) {
			fail(String.format("all messages must be delivered (remains %s)", failed.getCount()));
		}
	}

	@Test
	public void testTakeAndBuffer() throws InterruptedException {
		// given
		variant = new AndroidVariant();
		delivered = new CountDownLatch(NUMBER_OF_MESSAGES);

		eventProcessor.take(1).repeat().subscribe(s -> emulateMessageProcessingForRedelivery(s));

		// when - Adding DEFAULT_QUEUE_SIZE messages
		for (int i = 1; i <= NUMBER_OF_MESSAGES; i++) {
			eventProcessor.onNext(new MessageHolderWithTokens(information, message, variant, deviceTokens, i));
		}

		// then
		if (!delivered.await(DEFAULT_WAIT, TimeUnit.SECONDS)) {
			fail(String.format("all messages must be delivered (remains %s)", delivered.getCount()));
		}

		if (delivered.getCount() != 0) {
			fail(String.format("all messages must be delivered (remains %s)", delivered.getCount()));
		}
	}

	@Test
	public void tesQueueLimit() throws InterruptedException {
		// given
		variant = new SimplePushVariant();
		failed = new CountDownLatch(DEFAULT_QUEUE_SIZE);

		// when - Adding one more message will block this thread.
		for (int i = 1; i <= DEFAULT_QUEUE_SIZE; i++) {
			if (eventProcessor.getAvailableCapacity() <= 0){
				fail(String.format("Event Processor is not alive"));
			}

			eventProcessor.onNext(new MessageHolderWithTokens(information, message, variant, deviceTokens, i));
		}

		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			eventProcessor.take(10).repeat().subscribe(s -> emulateRedeliverableMessageProcessing(s));
		}

		// then
		if (!failed.await(DEFAULT_WAIT, TimeUnit.SECONDS)) {
			fail(String.format("all messages must be delivered (remains %s)", failed.getCount()));
		}
	}

	public void emulateMessageProcessingForRedelivery(MessageHolderWithTokens msg) {
		if (msg.getVariant() instanceof AndroidVariant) {
			logger.info("success #" + msg.getSerialId() + " " + Thread.currentThread().getName());
			delivered.countDown();
		}
	}

	public void emulateRedeliverableMessageProcessing(MessageHolderWithTokens msg) {
		if (failed.getCount() == 0)
			throw new RuntimeException();
		logger.info("success #" + msg.getSerialId() + " " + Thread.currentThread().getName());
		failed.countDown();
	}

	public Flux<MessageHolderWithTokens> handleIllegalStateException(Throwable e,
			WorkQueueProcessor<MessageHolderWithTokens> event) {
		return event;
	}

}
