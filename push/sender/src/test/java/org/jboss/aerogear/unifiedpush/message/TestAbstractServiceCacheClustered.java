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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.cache.ApnsServiceCache;
import org.jboss.aerogear.unifiedpush.message.cache.ServiceConstructor;
import org.jboss.aerogear.unifiedpush.message.cache.ServiceDestroyer;
import org.jboss.aerogear.unifiedpush.message.jms.util.JMSExecutor;
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
public class TestAbstractServiceCacheClustered {

    private static final String PUSH_MESSAGE_ID = TestAbstractServiceCacheClustered.class.getName();
    private static final String VARIANT_ID = TestAbstractServiceCacheClustered.class.getName();

    @Deployment(name = "war-1") @TargetsContainer("container-1")
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestAbstractServiceCacheClustered.class)
                .withMessaging()
                    .addPackage(org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache.class.getPackage())
                    .deleteClass(ApnsServiceCache.class)
                    .addClasses(MockServiceCacheForCluster.class)
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
    private MockServiceCacheForCluster cache;

    @Resource(mappedName = "java:/queue/CountingQueue")
    private Queue countingQueue;

    private static final int TOTAL_COUNT = 5;

    @Inject
    private JMSExecutor jmsExecutor;

    @Before
    public void setUp() {
        mockConstructor = new ServiceConstructor<Integer>() {
            @Override
            public Integer construct() {
                return jmsExecutor.receive(countingQueue);
            }
        };
        mockDestroyer = new ServiceDestroyer<Integer>() {
            @Override
            public void destroy(Integer serviceNumber) {
                jmsExecutor.send(countingQueue, serviceNumber);
            }
        };
    }

    @Test @OperateOnDeployment("war-1") @InSequence(1)
    public void initializeQueuesOnFirstNode() throws InterruptedException {
        // initialize cache with the badges
        cache.initialize(PUSH_MESSAGE_ID, VARIANT_ID);
        // remove all numbers from counting queue so that we start test with blank slate (in case of previous errors)
        for (int i = 1; i <= TOTAL_COUNT; i++) {
            if (jmsExecutor.receive(countingQueue, 1500L) == null) {
                break;
            }
        }
        // then push new numbers to counting queue
        for (int i = 1; i <= TOTAL_COUNT; i++) {
            jmsExecutor.send(countingQueue, new Integer(i));
        }
    }

    @Test @OperateOnDeployment("war-1") @InSequence(2)
    public void testLeaseFewNumbers() throws InterruptedException {
        assertEquals(1, leaseService());
        assertEquals(2, leaseService());
        assertEquals(3, leaseService());
    }

    @Test @OperateOnDeployment("war-2") @InSequence(3)
    public void testLeaseTwoAndReturnOne() throws InterruptedException {
        assertEquals(4, leaseService());
        assertEquals(5, leaseService());
        assertEquals("the sixth lease operation should fail to retrieve a service", 0, leaseService());
        returnService(4);
    }

    @Test @OperateOnDeployment("war-1") @InSequence(4)
    public void testLeaseOneAndReturnOne() throws InterruptedException {
        assertEquals(4, leaseService());
        returnService(1);
    }

    @Test @OperateOnDeployment("war-2") @InSequence(5)
    public void testReturnAndLease() throws InterruptedException {
        returnService(5);
        Set<Integer> leased = new HashSet<Integer>(Arrays.asList(leaseService(), leaseService()));
        assertTrue(leased.contains(1));
        assertTrue(leased.contains(5));
        assertEquals("the lease operation should fail when no badges are available", 0, leaseService());
    }

    private int leaseService() {
        Integer leased = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor);
        return (leased == null) ? 0 : leased;
    }

    private void returnService(int service) {
        cache.queueFreedUpService(PUSH_MESSAGE_ID, VARIANT_ID, service, mockDestroyer);
    }
}
