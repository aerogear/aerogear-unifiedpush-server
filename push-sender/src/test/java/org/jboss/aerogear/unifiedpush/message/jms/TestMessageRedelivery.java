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

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.FlatPushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSTokenVariant;
import org.jboss.aerogear.unifiedpush.message.MockProviders;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.exception.PushNetworkUnreachableException;
import org.jboss.aerogear.unifiedpush.message.exception.SenderResourceNotAvailableException;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.util.QueueUtils;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class TestMessageRedelivery {

    private static final int NUMBER_OF_MESSAGES = 10;
    private static final int RESOURCE_NOT_AVAILABLE = 3;
    private static final int PUSH_NETWORK_UNREACHABLE = 3;

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestMessageRedelivery.class)
                .withMessaging()
                .withMessageDrivenBeans()
                .withMockito()
                .addClass(QueueUtils.class)
                .addClasses(MockProviders.class)
                .as(WebArchive.class);
    }

    private UnifiedPushMessage message;
    private FlatPushMessageInformation information;
    private Variant variant;
    private Collection<String> deviceTokens;

    private static CountDownLatch delivered;
    private static CountDownLatch resourceNotAvailable;
    private static CountDownLatch pushNetworkUnreachable;
    private static CountDownLatch failed;

    private static final AtomicInteger counter = new AtomicInteger(0);

    @Inject @DispatchToQueue
    private Event<MessageHolderWithTokens> event;

    @Before
    public void setUp() {
        information = new FlatPushMessageInformation();
        message = new UnifiedPushMessage();
        deviceTokens = new ArrayList<>();
    }

    @Test
    public void testMessageWillBeRedelivered() throws InterruptedException {
        // given
        variant = new AndroidVariant();
        delivered = new CountDownLatch(NUMBER_OF_MESSAGES);
        resourceNotAvailable = new CountDownLatch(RESOURCE_NOT_AVAILABLE);
        pushNetworkUnreachable = new CountDownLatch(PUSH_NETWORK_UNREACHABLE);
        counter.set(0);

        // when
        for (int i = 1; i <= NUMBER_OF_MESSAGES; i++) {
            event.fire(new MessageHolderWithTokens(information, message, variant, deviceTokens, i));
        }

        // then
        if (!resourceNotAvailable.await(RESOURCE_NOT_AVAILABLE + 1, TimeUnit.SECONDS)) {
            fail(String.format("%s tries must time out on resource establishing (remains %s)", RESOURCE_NOT_AVAILABLE, resourceNotAvailable.getCount()));
        }

        if (!resourceNotAvailable.await(RESOURCE_NOT_AVAILABLE + 1, TimeUnit.SECONDS)) {
            fail(String.format("%s tries must fail to initiate connection (remains %s)", PUSH_NETWORK_UNREACHABLE, pushNetworkUnreachable.getCount()));
        }

        if (!delivered.await(NUMBER_OF_MESSAGES + 1, TimeUnit.SECONDS)) {
            fail(String.format("all messages must be delivered (remains %s)", delivered.getCount()));
        }
    }

    @Test
    public void testMessageCannotBeRedelivered() throws InterruptedException {
        // given
        variant = new iOSTokenVariant();
        failed = new CountDownLatch(NUMBER_OF_MESSAGES);
        counter.set(0);

        // when
        for (int i = 1; i <= NUMBER_OF_MESSAGES; i++) {
            event.fire(new MessageHolderWithTokens(information, message, variant, deviceTokens, i));
        }

        // then
        if (!failed.await(NUMBER_OF_MESSAGES + 1, TimeUnit.SECONDS)) {
            fail(String.format("all messages must fail to be delivered (remains %s)", failed.getCount()));
        }

    }

    public void emulateMessageProcessingForRedelivery(@Observes @Dequeue MessageHolderWithTokens msg) {
        if (msg.getVariant() instanceof AndroidVariant) {
            int count = counter.incrementAndGet();

            System.out.println("starting #" + count);
            if (count <= RESOURCE_NOT_AVAILABLE) {
                System.out.println("resource not available #" + count);
                resourceNotAvailable.countDown();
                throw new SenderResourceNotAvailableException("Resource not available");
            } else if (count <= PUSH_NETWORK_UNREACHABLE + RESOURCE_NOT_AVAILABLE) {
                System.out.println("communication failure #" + count);
                pushNetworkUnreachable.countDown();
                throw new PushNetworkUnreachableException("Communication failure");
            } else {
                System.out.println("sent #" + count);
                delivered.countDown();
            }
        }
    }

    public void emulateNonRedeliverableMessageProcessing(@Observes @Dequeue MessageHolderWithTokens msg) {
        if (msg.getVariant() instanceof iOSTokenVariant) {
            int count = counter.incrementAndGet();
            System.out.println("fail #" + count);
            failed.countDown();
            throw new IllegalStateException("The messaging should not try to re-deliver this message");
        }
    }

}
