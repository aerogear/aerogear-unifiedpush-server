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

import javax.annotation.Resource;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestBadgeLeasing extends AbstractJMSTest {

    private final static String PAYLOAD = "hello there";

    @Deployment(name = "war-1") @TargetsContainer("container-1")
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestBadgeLeasing.class)
                .withMessaging()
                .withMockito()
                .as(WebArchive.class);
    }

    @Deployment(name = "war-2") @TargetsContainer("container-2")
    public static WebArchive archive2() {
        return archive();
    }

    @Resource(mappedName = "java:/queue/APNsBadgeLeaseQueue")
    private Queue apnsBadgeLeaseQueue;

    @Test @OperateOnDeployment("war-1") @InSequence(1)
    public void testSendTenAndReceiveFive() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            send(apnsBadgeLeaseQueue, PAYLOAD);
        }
        testReceiveFive();
    }

    @Test @OperateOnDeployment("war-2") @InSequence(2)
    public void testReceiveFive() throws InterruptedException {
        for (int i = 0; i < 5; i++) {
            assertEquals(PAYLOAD, receive(apnsBadgeLeaseQueue));
        }
    }

}
