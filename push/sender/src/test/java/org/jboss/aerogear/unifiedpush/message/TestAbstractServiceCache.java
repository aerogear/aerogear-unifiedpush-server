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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache;
import org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache.ServiceConstructor;
import org.junit.Before;
import org.junit.Test;

public class TestAbstractServiceCache {

    private static final int INSTANCE_LIMIT = 5;
    private static final long INSTANTIATION_TIMEOUT = 200;

    private static final String PUSH_MESSAGE_ID = UUID.randomUUID().toString();
    private static final String VARIANT_ID = UUID.randomUUID().toString();

    private ServiceConstructor<Integer> mockConstructor;
    private AtomicInteger instanceCounter = new AtomicInteger();

    @Before
    public void setUp() {
        mockConstructor = new ServiceConstructor<Integer>() {
            @Override
            public Integer construct() {
                return instanceCounter.incrementAndGet();
            }
        };
    }

    @Test
    public void allows_to_free_up_slots() {
        MockServiceCache cache = new MockServiceCache();
        for (Integer i = 1; i <= INSTANCE_LIMIT - 1; i++) {
            assertNotNull(cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
            assertEquals(new Integer(i * 2), cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
            cache.freeUpSlot(PUSH_MESSAGE_ID, VARIANT_ID);
        }
        for (Integer i = 1; i <= INSTANCE_LIMIT - 1; i++) {
            cache.freeUpSlot(PUSH_MESSAGE_ID, VARIANT_ID);
        }
        try {
            cache.freeUpSlot(PUSH_MESSAGE_ID, VARIANT_ID);
            fail("should throw an exception when slot counter is lesser than zero");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void allows_to_return_freed_up_services_to_queue() {
        MockServiceCache cache = new MockServiceCache();
        for (Integer i = 1; i <= INSTANCE_LIMIT - 1; i++) {
            Integer service = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor);
            assertEquals(i, service);
            cache.queueFreedUpService(PUSH_MESSAGE_ID, VARIANT_ID, service);
            service = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor);
            assertEquals(i, service);
        }
    }

    @Test
    public void returns_null_when_no_slots_available() {
        MockServiceCache cache = new MockServiceCache();
        for (Integer i = 1; i <= INSTANCE_LIMIT; i++) {
            assertEquals(i, cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
        }
        assertNull(cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor));
    }

    @Test
    public void cache_is_blocking_until_service_is_available() throws InterruptedException {
        final int threadCount = 4;
        final MockServiceCache cache = new MockServiceCache();
        final CountDownLatch latch = new CountDownLatch(threadCount * INSTANCE_LIMIT);
        for (int i = 0; i < threadCount ; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < INSTANCE_LIMIT; i++) {
                        Integer service = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, VARIANT_ID, mockConstructor);
                        System.out.println(Thread.currentThread().getName() + ": #" + service);
                        sleep(INSTANTIATION_TIMEOUT / (threadCount * 2));
                        cache.queueFreedUpService(PUSH_MESSAGE_ID, VARIANT_ID, service);
                        if (service != null) {
                            latch.countDown();
                        }
                        sleep(INSTANTIATION_TIMEOUT / (threadCount * 2));
                    }
                }
            }).start();
        }
        if (!latch.await(INSTANTIATION_TIMEOUT * threadCount * 2, TimeUnit.MILLISECONDS)) {
            fail("All threads didnt finish");
        }
    }

    @Test
    public void allows_to_call_dequeue_when_no_instance_was_created() {
        final MockServiceCache cache = new MockServiceCache();
        assertNull(cache.dequeue("non-existent", "non-existent"));
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class MockServiceCache extends AbstractServiceCache<Integer> {



        public MockServiceCache() {
            super(INSTANCE_LIMIT, INSTANTIATION_TIMEOUT);
        }

    }
}
