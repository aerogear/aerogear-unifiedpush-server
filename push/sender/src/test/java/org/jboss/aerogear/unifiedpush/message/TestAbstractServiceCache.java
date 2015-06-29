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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.serviceLease.ApnsServiceHolder;
import org.jboss.aerogear.unifiedpush.message.serviceLease.ServiceConstructor;
import org.jboss.aerogear.unifiedpush.message.serviceLease.ServiceDestroyer;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestAbstractServiceCache {

    private static final int INSTANCE_LIMIT = 5;
    private static final long INSTANTIATION_TIMEOUT = 200;

    private static final String PUSH_MESSAGE_ID = UUID.randomUUID().toString();
    private static final String VARIANT_ID = UUID.randomUUID().toString();

    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(TestAbstractServiceCache.class)
                .withMessaging()
                    .addPackage(org.jboss.aerogear.unifiedpush.message.serviceLease.AbstractServiceHolder.class.getPackage())
                    .deleteClass(ApnsServiceHolder.class)
                    .addClasses(MockServiceCacheForSingleNode.class)
                .withMockito()
                .as(WebArchive.class);
    }

    private ServiceConstructor<Integer> mockConstructor;
    private ServiceDestroyer<Integer> mockDestroyed;
    private AtomicInteger instanceCounter = new AtomicInteger();

    @Inject
    private MockServiceCacheForSingleNode cache;

    @Resource(mappedName = "java:/queue/APNsBadgeLeaseQueue")
    private Queue queue;

    @Before
    public void setUp() {
        cache.initialize(PUSH_MESSAGE_ID, VARIANT_ID);
        mockConstructor = new ServiceConstructor<Integer>() {
            @Override
            public Integer construct() {
                return instanceCounter.incrementAndGet();
            }
        };
        mockDestroyed = new ServiceDestroyer<Integer>() {
            @Override
            public void destroy(Integer instance) {
            }
        };
    }

    @After
    public void tearDown() {
        cache.destroy(PUSH_MESSAGE_ID, VARIANT_ID);
    }

    @Test
    public void allows_to_free_up_slots() throws ExecutionException {
        for (Integer i = 1; i <= INSTANCE_LIMIT - 1; i++) {
            // create first service
            assertNotNull(cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
            // create second service
            assertEquals(new Integer(i * 2), cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
            // simulate that second service has died
            cache.freeUpSlot(PUSH_MESSAGE_ID, VARIANT_ID);
        }
        for (Integer i = 1; i <= INSTANCE_LIMIT - 1; i++) {
            // simulate that services died
            cache.freeUpSlot(PUSH_MESSAGE_ID, VARIANT_ID);
        }
        for (Integer i = 1; i <= INSTANCE_LIMIT; i++) {
            // since all previous services died, we can now create up to 5 services again
            assertNotNull(cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
        }
        assertNull("No more services should be created", cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
    }

    @Test
    public void allows_to_return_freed_up_services_to_queue() throws ExecutionException {
        for (Integer i = 1; i <= INSTANCE_LIMIT - 1; i++) {
            Integer service = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor);
            assertEquals(i, service);
            cache.queueFreedUpService(PUSH_MESSAGE_ID, VARIANT_ID, service, mockDestroyed);
            service = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor);
            assertEquals(i, service);
        }
    }

    @Test
    public void returns_null_when_no_slots_available() throws ExecutionException {
        for (Integer i = 1; i <= INSTANCE_LIMIT; i++) {
            assertEquals(i, cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
        }
        assertNull(cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
    }

    @Test
    public void cache_is_blocking_until_service_is_available() throws InterruptedException {
        final int threadCount = 4;
        final CountDownLatch latch = new CountDownLatch(threadCount * INSTANCE_LIMIT);
        for (int i = 0; i < threadCount ; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < INSTANCE_LIMIT; i++) {
                            Integer service = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor);
                            sleep(INSTANTIATION_TIMEOUT / (threadCount * 2));
                            cache.queueFreedUpService(PUSH_MESSAGE_ID, VARIANT_ID, service, mockDestroyed);
                            if (service != null) {
                                latch.countDown();
                            }
                            sleep(INSTANTIATION_TIMEOUT / (threadCount * 2));
                        }
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
            }).start();
        }
        if (!latch.await(INSTANTIATION_TIMEOUT * threadCount * 2, TimeUnit.MILLISECONDS)) {
            fail("All threads didnt finish");
        }
    }

    @Test
    public void allows_to_call_dequeue_when_no_instance_was_created() throws ExecutionException {
        assertNull(cache.dequeue("non-existent", "non-existent"));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
