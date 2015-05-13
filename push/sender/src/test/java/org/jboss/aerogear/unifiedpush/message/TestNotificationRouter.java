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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;


@RunWith(Arquillian.class)
public class TestNotificationRouter {

    @Deployment
    public static WebArchive archive() {
        return ShrinkWrap
                .create(UnifiedPushArchive.class)
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
    private Counter messageCounter;

    private PushApplication app;
    private InternalUnifiedPushMessage message;

    @Before
    public void setUp() {
        app = new PushApplication();
        message = new InternalUnifiedPushMessage();
    }

    @Test
    public void testNoVariants() {
        assertTrue("variants are empty", app.getVariants().isEmpty());
        router.submit(app, message);
        assertEquals("when variants are empty, no message is dispatched", 0, messageCounter.getCounter());
    }

    @Test
    public void testTwoVariantsOfSameType() {
        app.getVariants().add(new SimplePushVariant());
        app.getVariants().add(new SimplePushVariant());
        router.submit(app, message);
        assertEquals("the message should be dispatched", 1, messageCounter.getCounter());
    }

    @Test
    public void testThreeVariantsOfDifferentType() {
        app.getVariants().add(new AndroidVariant());
        app.getVariants().add(new iOSVariant());
        app.getVariants().add(new SimplePushVariant());
        router.submit(app, message);
        assertEquals("the 3 messages should be dispatched", 3, messageCounter.getCounter());
    }

    @Test
    public void testInvokesMetricsService(PushMessageInformationDao pushMessageInformationDao) {
        router.submit(app, message);
        verify(pushMessageInformationDao).create(Mockito.any(PushMessageInformation.class));
    }

    @Test
    public void testVariantIDsSpecified(GenericVariantService genericVariantService) {
        // given
        SimplePushVariant simplePushVariant = new SimplePushVariant();
        simplePushVariant.setId("id-simplepush-variant");
        iOSVariant iOSVariant = new iOSVariant();
        iOSVariant.setId("id-ios-variant");
        AndroidVariant androidVariant = new AndroidVariant();
        androidVariant.setId("id-android-variant");

        app.getVariants().addAll(Arrays.asList(simplePushVariant, iOSVariant, androidVariant));
        message.getCriteria().setVariants(Arrays.asList("id-ios-variant", "id-android-variant"));

        when(genericVariantService.findByVariantID("id-ios-variant")).thenReturn(iOSVariant);
        when(genericVariantService.findByVariantID("id-android-variant")).thenReturn(androidVariant);
        when(genericVariantService.findByVariantID("id-simplepush-variant")).thenReturn(androidVariant);

        // when
        router.submit(app, message);
        assertEquals("the 2 messages should be dispatched", 2, messageCounter.getCounter());
    }

    public void observeMessageHolderWithVariants(@Observes @DispatchToQueue MessageHolderWithVariants msg) {
        messageCounter.increment();
    }

    @RequestScoped
    public static class Counter {
        private volatile int counter = 0;
        public void increment() {
            counter += 1;
        }
        public int getCounter() {
            return counter;
        }
    }


}
