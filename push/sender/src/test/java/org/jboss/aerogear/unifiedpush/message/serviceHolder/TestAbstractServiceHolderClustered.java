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
package org.jboss.aerogear.unifiedpush.message.serviceHolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.serviceHolder.ApnsServiceHolder;
import org.jboss.aerogear.unifiedpush.message.serviceHolder.ServiceConstructor;
import org.jboss.aerogear.unifiedpush.message.serviceHolder.ServiceDestroyer;
import org.jboss.aerogear.unifiedpush.message.util.JmsClient;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestAbstractServiceHolderClustered {

    private static final String PUSH_MESSAGE_ID = TestAbstractServiceHolderClustered.class.getName();
    private static final String VARIANT_ID = TestAbstractServiceHolderClustered.class.getName();

    @Deployment(name = "war-1") @TargetsContainer("container-1")
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestAbstractServiceHolderClustered.class)
                .withMessaging()
                    .addPackage(org.jboss.aerogear.unifiedpush.message.serviceHolder.AbstractServiceHolder.class.getPackage())
                    .deleteClass(ApnsServiceHolder.class)
                    .addClasses(MockServiceHolderForCluster.class)
                    .addAsWebInfResource("test-jms.xml")
                .withMockito()
                .as(WebArchive.class);
    }

    @Deployment(name = "war-2") @TargetsContainer("container-2")
    public static WebArchive archive2() {
        return archive();
    }

    private ServiceConstructor<Integer> mockConstructor;
    private ServiceDestroyer<Integer> mockDestroyer;

    @Inject
    private MockServiceHolderForCluster holder;

    @Resource(mappedName = "java:/queue/CountingQueue")
    private Queue countingQueue;

    private static final int TOTAL_COUNT = 5;

    @Inject
    private JmsClient jmsClient;

    @Before
    public void setUp() {
        mockConstructor = new ServiceConstructor<Integer>() {
            @Override
            public Integer construct() {
                try {
                    return (Integer) jmsClient.receive().from(countingQueue).getObject();
                } catch (JMSException e) {
                    e.printStackTrace();
                    throw new IllegalStateException(e);
                }
            }
        };
        mockDestroyer = new ServiceDestroyer<Integer>() {
            @Override
            public void destroy(Integer serviceNumber) {
                jmsClient.send(serviceNumber).to(countingQueue);
            }
        };
    }

    @Test @OperateOnDeployment("war-1") @InSequence(1)
    public void initializeQueuesOnFirstNode() throws InterruptedException {
        // initialize holder with the service slots
        holder.initialize(PUSH_MESSAGE_ID, VARIANT_ID);
        // remove all numbers from counting queue so that we start test with blank slate (in case of previous errors)
        for (int i = 1; i <= TOTAL_COUNT; i++) {
            if (jmsClient.receive().withTimeout(1500L).from(countingQueue) == null) {
                break;
            }
        }
        // then push new numbers to counting queue
        for (int i = 1; i <= TOTAL_COUNT; i++) {
            jmsClient.send(new Integer(i)).to(countingQueue);
        }
    }

    @Test @OperateOnDeployment("war-1") @InSequence(2)
    public void testLeaseFewNumbers() throws InterruptedException {
        assertEquals(1, borrowServiceSlot());
        assertEquals(2, borrowServiceSlot());
        assertEquals(3, borrowServiceSlot());
    }

    @Test @OperateOnDeployment("war-2") @InSequence(3)
    public void testLeaseTwoAndReturnOne() throws InterruptedException {
        assertEquals(4, borrowServiceSlot());
        assertEquals(5, borrowServiceSlot());
        assertEquals("the sixth lease operation should fail to retrieve a service", 0, borrowServiceSlot());
        returnServiceSlot(4);
    }

    @Test @OperateOnDeployment("war-1") @InSequence(4)
    public void testLeaseOneAndReturnOne() throws InterruptedException {
        assertEquals(4, borrowServiceSlot());
        returnServiceSlot(1);
    }

    @Test @OperateOnDeployment("war-2") @InSequence(5)
    public void testReturnAndLease() throws InterruptedException {
        returnServiceSlot(5);
        Set<Integer> leased = new HashSet<>(Arrays.asList(borrowServiceSlot(), borrowServiceSlot()));
        assertTrue("leased tokens should contain 1", leased.contains(1));
        assertTrue("leased tokens should contain 5", leased.contains(5));
        assertEquals("the borrow operation should fail when no slots are available", 0, borrowServiceSlot());
    }

    private int borrowServiceSlot() {
        Integer borrowed = holder.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor);
        return (borrowed == null) ? 0 : borrowed;
    }

    private void returnServiceSlot(int service) {
        holder.queueFreedUpService(PUSH_MESSAGE_ID, VARIANT_ID, service, mockDestroyer);
    }
}