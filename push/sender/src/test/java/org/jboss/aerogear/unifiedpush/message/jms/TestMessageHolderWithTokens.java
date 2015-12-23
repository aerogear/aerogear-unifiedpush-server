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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.MockProviders;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushSenderArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class TestMessageHolderWithTokens {

    @Deployment
    public static WebArchive archive() {

        return UnifiedPushSenderArchive.forTestClass(TestMessageHolderWithTokens.class)
                .withMessaging()
                    .addClasses(MessageHolderWithTokensConsumer.class, MessageHolderWithTokensProducer.class, AbstractJMSMessageListener.class)
                    .addAsWebInfResource("jboss-ejb3-message-holder-with-tokens.xml", "jboss-ejb3.xml")
                .withMockito()
                    .addClasses(MockProviders.class)
                .as(WebArchive.class);
    }

    private UnifiedPushMessage message;
    private PushMessageInformation information;
    private Variant variant;
    private Collection<String> deviceTokens;
    private static CountDownLatch delivered;

    @Inject @DispatchToQueue
    private Event<MessageHolderWithTokens> event;

    @Before
    public void setUp() {
        information = new PushMessageInformation();
        message = new UnifiedPushMessage();
        deviceTokens = new ArrayList<String>();
        delivered = new CountDownLatch(5);
    }

    @Test
    public void test() throws InterruptedException {
        variant = new AndroidVariant();
        for (int i = 0; i < 5; i++) {
            event.fire(new MessageHolderWithTokens(information, message, variant, deviceTokens, i));
        }
        delivered.await(5, TimeUnit.SECONDS);
    }

    public void observeMessageHolderWithVariants(@Observes @Dequeue MessageHolderWithTokens msg) {
        delivered.countDown();
    }

}
