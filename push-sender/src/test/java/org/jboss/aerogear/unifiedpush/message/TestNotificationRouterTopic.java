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
package org.jboss.aerogear.unifiedpush.message;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.junit.Before;
import org.junit.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.TopicProcessor;

public class TestNotificationRouterTopic {
	private static final int NUMBER_OF_MESSAGES = 1000;
	private static final int DEFAULT_QUEUE_SIZE = 256;

	private TopicProcessor<MessageHolderWithVariants> topic;
	private static CountDownLatch countDownLatch;

	@Before
	public void specificSetup() {
		topic = TopicProcessor.<MessageHolderWithVariants>builder().build();

	}

	@Test
	public void test200MessagesBeforeSubscribe() throws InterruptedException {
		countDownLatch = new CountDownLatch(DEFAULT_QUEUE_SIZE);

		for (int i = 0; i < DEFAULT_QUEUE_SIZE; i++) {
			topic.onNext(new MessageHolderWithVariants(null, null, null, new ArrayList<>()));
		}

		topic.repeat().subscribe(s -> getMessage(s));

		countDownLatch.await(1, TimeUnit.SECONDS);

		assertEquals(0, countDownLatch.getCount());
	}

	@Test
	public void test1000MessagesBeforeSubscribe() throws InterruptedException {
		countDownLatch = new CountDownLatch(NUMBER_OF_MESSAGES * 3);

		// Add X first messages
		for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			topic.onNext(new MessageHolderWithVariants(null, null, null, new ArrayList<>()));
		}

		// Subscribe
		topic.repeat().subscribe(s -> getMessage(s));

		// Add X additional messages
		for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			topic.onNext(new MessageHolderWithVariants(null, null, null, new ArrayList<>()));
		}

		// Add X additional messages
		for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			topic.onNext(new MessageHolderWithVariants(null, null, null, new ArrayList<>()));
		}

		countDownLatch.await(3, TimeUnit.SECONDS);

		assertEquals(0, countDownLatch.getCount());
	}


	@Test
	public void test1000MessagesTakeX() throws InterruptedException {
		countDownLatch = new CountDownLatch(NUMBER_OF_MESSAGES * 3);

		// Add X first messages
		for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			topic.onNext(new MessageHolderWithVariants(null, null, null, new ArrayList<>()));
		}

		// Subscribe
		topic.take(NUMBER_OF_MESSAGES).repeat().subscribe(s -> getMessage(s));


		Thread.sleep(200); // Wait for Flux to repeat
		// Add X additional messages
		for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			topic.onNext(new MessageHolderWithVariants(null, null, null, new ArrayList<>()));
		}

		Thread.sleep(200); // Wait for Flux to repeat
		// Add X additional messages
		for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
			topic.onNext(new MessageHolderWithVariants(null, null, null, new ArrayList<>()));
		}

		countDownLatch.await(3, TimeUnit.SECONDS);

		assertEquals(0, countDownLatch.getCount());
	}

	public void testRecoverFromError() throws InterruptedException {
		countDownLatch = new CountDownLatch(DEFAULT_QUEUE_SIZE);

		for (int i = 0; i < DEFAULT_QUEUE_SIZE; i++) {
			topic.onNext(new MessageHolderWithVariants(null, null, null, new ArrayList<>()));
		}

		topic.repeat().onErrorResume(e -> getMessageWithException(e, topic)).subscribe(s -> getMessageWithException(s));

		countDownLatch.await(3, TimeUnit.SECONDS);
		assertEquals(0, countDownLatch.getCount());
	}

	public void getMessage(MessageHolderWithVariants message) {
		if (countDownLatch.getCount() == 0)
			throw new RuntimeException();
		countDownLatch.countDown();
	}

	public void getMessageWithException(MessageHolderWithVariants message) {
		if (countDownLatch.getCount() == 100)
			throw new RuntimeException();
		countDownLatch.countDown();
	}

	public Flux<MessageHolderWithVariants> getMessageWithException(Throwable e, Flux<MessageHolderWithVariants> fallback) {
		System.out.println("ERROR");
		return topic.repeat().onErrorResume(ex-> getMessageWithException(ex, topic));
	}

}
