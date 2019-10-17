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

import org.jboss.aerogear.unifiedpush.api.*;
import org.jboss.aerogear.unifiedpush.dao.FlatPushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Arquillian.class)
public class TestNotificationRouter {


    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestNotificationRouter.class)
                .addClass(TestNotificationRouter.class)
                .withMessaging()
                    .addClasses(NotificationRouter.class, PushNotificationSender.class)
                    .addClasses(PushMessageMetricsService.class)
                .withMockito()
                    .addClasses(MockProviders.class)
                .as(WebArchive.class);
    }

    @Inject
    private NotificationRouter router;

    @Inject
    private VariantTypesHolder variantTypeHolder;

    private static CountDownLatch countDownLatch;

    private PushApplication app;
    private InternalUnifiedPushMessage message;

    @Before
    public void setUp() {
        app = new PushApplication();
        message = new InternalUnifiedPushMessage();
    }

    @Test
    public void testNoVariants() {
        countDownLatch = new CountDownLatch(1);
        assertTrue("variants are empty", app.getVariants().isEmpty());
        router.submit(app, message);
        assertEquals(variants(), variantTypeHolder.getVariantTypes());
    }

    @Test
    public void testTwoVariantsOfSameType() throws InterruptedException {
        countDownLatch = new CountDownLatch(1);
        app.getVariants().add(new AndroidVariant());
        app.getVariants().add(new AndroidVariant());
        router.submit(app, message);
        countDownLatch.await(3, TimeUnit.SECONDS);
        assertEquals(variants(VariantType.ANDROID), variantTypeHolder.getVariantTypes());
    }

    @Test
    public void testThreeVariantsOfDifferentType() throws InterruptedException {
        countDownLatch = new CountDownLatch(3);
        app.getVariants().add(new AndroidVariant());
        app.getVariants().add(new iOSVariant());
        router.submit(app, message);
        countDownLatch.await(3, TimeUnit.SECONDS);
        assertEquals(variants(VariantType.ANDROID, VariantType.IOS), variantTypeHolder.getVariantTypes());
    }

    @Test
    public void testInvokesMetricsService(FlatPushMessageInformationDao pushMessageInformationDao) {
        router.submit(app, message);
        verify(pushMessageInformationDao).create(Mockito.any(FlatPushMessageInformation.class));
    }

    @Test
    public void testVariantIDsSpecified(GenericVariantService genericVariantService) throws InterruptedException {
        // given
        countDownLatch = new CountDownLatch(2);
        iOSVariant iOSVariant = new iOSVariant();
        iOSVariant.setId("id-ios-variant");
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setId("id-android-variant");

        app.getVariants().addAll(Arrays.asList(iOSVariant, androidVariant));
        message.getCriteria().setVariants(Arrays.asList("id-ios-variant", "id-android-variant"));

        when(genericVariantService.findByVariantID("id-ios-variant")).thenReturn(iOSVariant);
        when(genericVariantService.findByVariantID("id-android-variant")).thenReturn(androidVariant);

        // when
        router.submit(app, message);
        countDownLatch.await(3, TimeUnit.SECONDS);
        assertEquals(variants(VariantType.ANDROID, VariantType.IOS), variantTypeHolder.getVariantTypes());
    }

    public void observeMessageHolderWithVariants(@Observes @DispatchToQueue MessageHolderWithVariants msg) {
        variantTypeHolder.addVariantType(msg.getVariantType());
        countDownLatch.countDown();
    }

    @RequestScoped
    public static class VariantTypesHolder {
        private Set<VariantType> variantTypes = new HashSet<>();

        public void addVariantType(VariantType variantType) {
            this.variantTypes.add(variantType);
        }
        public Set<VariantType> getVariantTypes() {
            return variantTypes;
        }
    }

    private Set<VariantType> variants(VariantType... types) {
        return new HashSet<>(Arrays.asList(types));
    }


}
