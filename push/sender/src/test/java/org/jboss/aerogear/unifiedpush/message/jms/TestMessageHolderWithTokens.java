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
import org.jboss.aerogear.unifiedpush.message.Config;
import org.jboss.aerogear.unifiedpush.message.Criteria;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.MessageDeliveryException;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class TestMessageHolderWithTokens {

    @Deployment
    public static WebArchive archive() {

        String[] libs = new String[] {
                "org.mockito:mockito-core",
                "org.codehaus.jackson:jackson-mapper-asl"
        };

        return ShrinkWrap
                .create(WebArchive.class)
                .addAsLibraries(Maven.resolver().loadPomFromFile("pom.xml").resolve(libs).withTransitivity().as(JavaArchive.class))
                .addClasses(MessageHolderWithTokens.class, DispatchToQueue.class, Dequeue.class, MessageDeliveryException.class)
                .addClasses(MessageHolderWithTokensConsumer.class, MessageHolderWithTokensProducer.class, AbstractJMSMessageConsumer.class)
                .addClasses(UnifiedPushMessage.class, Config.class, Criteria.class, Message.class)
                .addPackage(org.jboss.aerogear.unifiedpush.utils.AeroGearLogger.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.api.PushApplication.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.message.holder.AbstractMessageHolder.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.dao.PushApplicationDao.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.service.PushApplicationService.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.message.windows.Windows.class.getPackage())
                .addPackage(org.jboss.aerogear.unifiedpush.message.apns.APNs.class.getPackage())
                .addAsWebInfResource("hornetq-jms.xml")
                .addAsWebInfResource("jboss-ejb3-message-holder-with-tokens.xml", "jboss-ejb3.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
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
            event.fire(new MessageHolderWithTokens(information, message, variant, deviceTokens));
        }
        delivered.await(5, TimeUnit.SECONDS);
    }

    public void observeMessageHolderWithVariants(@Observes @Dequeue MessageHolderWithTokens msg) {
        delivered.countDown();
    }

}
