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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.dao.FlatPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.message.TestNotificationRouter.VariantTypesHolderConfig;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.service.AbstractNoCassandraServiceTest;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.TopicProcessor;

@ContextConfiguration(classes = { SenderConfig.class, VariantTypesHolderConfig.class })
public class TestNotificationRouter extends AbstractNoCassandraServiceTest {

	@Inject
	private NotificationRouter router;
	@Inject
	private VariantTypesHolder variantTypeHolder;
	@Inject
	private FlatPushMessageInformationDao pushMessageInformationDao;
	@Inject
	private GenericVariantService variantService;
	@Inject
	private TopicProcessor<MessageHolderWithVariants> nextBatchEvent;

	private static CountDownLatch countDownLatch;

	private PushApplication app;
	private InternalUnifiedPushMessage message;

	public void specificSetup() {
		app = new PushApplication();
		message = new InternalUnifiedPushMessage();
		variantTypeHolder.clear();

		if (nextBatchEvent.downstreamCount()==1)
			nextBatchEvent.take(Runtime.getRuntime().availableProcessors()).repeat()
					.subscribe(s -> variantTypeHolder.addVariantType(s.getVariantType()));
	}

	@Test
	public void testNoVariants() {
		countDownLatch = new CountDownLatch(1);
		assertTrue("variants are empty", app.getVariants().isEmpty());
		router.submit(app, message);
		assertEquals(variants(), variantTypeHolder.getVariantTypes());
	}

	@Test
	@Transactional
	public void testTwoVariantsOfSameType() throws InterruptedException {
		countDownLatch = new CountDownLatch(1);
		app.getVariants().add(new SimplePushVariant());
		app.getVariants().add(new SimplePushVariant());
		router.submit(app, message);
		countDownLatch.await(3, TimeUnit.SECONDS);
		assertEquals(variants(VariantType.SIMPLE_PUSH), variantTypeHolder.getVariantTypes());
	}

	@Test
	@Transactional
	public void testThreeVariantsOfDifferentType() throws InterruptedException {
		countDownLatch = new CountDownLatch(3);
		app.getVariants().add(new AndroidVariant());
		app.getVariants().add(new iOSVariant());
		app.getVariants().add(new SimplePushVariant());
		router.submit(app, message);
		countDownLatch.await(3, TimeUnit.SECONDS);
		assertEquals(variants(VariantType.ANDROID, VariantType.IOS, VariantType.SIMPLE_PUSH),
				variantTypeHolder.getVariantTypes());
	}

	@Test
	@Transactional
	public void testInvokesMetricsService() {
		router.submit(app, message);
		FlatPushMessageInformation messageInformation = new FlatPushMessageInformation();
		messageInformation.setPushApplicationId(app.getPushApplicationID());
		pushMessageInformationDao.create(messageInformation);
	}

	@Test
	@Transactional
	public void testVariantIDsSpecified() throws InterruptedException {
		// given
		countDownLatch = new CountDownLatch(2);

		SimplePushVariant simplePushVariant = new SimplePushVariant();
		simplePushVariant.setName("simplepush-variant");

		iOSVariant iOSVariant = new iOSVariant();
		iOSVariant.setName("ios-variant");
		try {
			iOSVariant.setCertificate(TestNotificationRouter.readCertificate("/cert/certificate.p12"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		iOSVariant.setPassphrase("123456");

		AndroidVariant androidVariant = new AndroidVariant();
		androidVariant.setName("android-variant");
		androidVariant.setGoogleKey("xxx-xxx-xxx-xxx");
		androidVariant.setProjectNumber("1234567890");

		variantService.addVariant(simplePushVariant, new LoggedInUser(DEFAULT_USER));
		variantService.addVariant(iOSVariant, new LoggedInUser(DEFAULT_USER));
		variantService.addVariant(androidVariant, new LoggedInUser(DEFAULT_USER));

		app.getVariants().addAll(Arrays.asList(simplePushVariant, iOSVariant, androidVariant));
		message.getCriteria().setVariants(Arrays.asList(iOSVariant.getVariantID(), androidVariant.getVariantID()));

		router.submit(app, message);
		countDownLatch.await(3, TimeUnit.SECONDS);
		assertEquals(variants(VariantType.ANDROID, VariantType.IOS), variantTypeHolder.getVariantTypes());
	}

	public static class VariantTypesHolderConfig {
		@Bean
		public VariantTypesHolder getVariantTypesHolder() {
			return new VariantTypesHolder();
		}
	}

	public static class VariantTypesHolder {
		private Set<VariantType> variantTypes = new HashSet<>();

		public void addVariantType(VariantType variantType) {
			this.variantTypes.add(variantType);
			countDownLatch.countDown();
		}

		public Set<VariantType> getVariantTypes() {
			return variantTypes;
		}

		public void clear() {
			variantTypes = new HashSet<>();
		}
	}

	private Set<VariantType> variants(VariantType... types) {
		return new HashSet<>(Arrays.asList(types));
	}

	/**
	 * The store read by this method was copied from
	 * https://github.com/notnoop/java-apns/tree/master/src/test/resources
	 */
	public static byte[] readCertificate(String cert) throws Exception {
		return asByteArray(TestNotificationRouter.class.getResourceAsStream(cert));
	}

	private static byte[] asByteArray(final InputStream is) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int reads = is.read();
		while (reads != -1) {
			baos.write(reads);
			reads = is.read();
		}
		return baos.toByteArray();
	}

}
