package org.jboss.aerogear.unifiedpush.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache;
import org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache.ServiceConstructor;
import org.junit.Before;
import org.junit.Test;

public class TestAbstractServiceCache {

    private static final int INSTANCE_LIMIT = 5;
    private static final long INSTANTIATION_TIMEOUT = 200;

    private static final String PUSH_MESSAGE_ID = UUID.randomUUID().toString();
    private static final String VARIANT_ID = UUID.randomUUID().toString();

    private Variant mockVariant;
    private ServiceConstructor<Integer> mockConstructor;
    private AtomicInteger instanceCounter = new AtomicInteger();

    @Before
    public void setUp() {
        mockVariant = mock(Variant.class);
        when(mockVariant.getVariantID()).thenReturn(VARIANT_ID);
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
            assertNotNull(cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, mockVariant, mockConstructor));
            assertEquals(new Integer(i * 2), cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, mockVariant, mockConstructor));
            cache.freeUpSlot(PUSH_MESSAGE_ID, mockVariant);
        }
        for (Integer i = 1; i <= INSTANCE_LIMIT - 1; i++) {
            cache.freeUpSlot(PUSH_MESSAGE_ID, mockVariant);
        }
        try {
            cache.freeUpSlot(PUSH_MESSAGE_ID, mockVariant);
            fail("should throw an exception when slot counter is lesser than zero");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void allows_to_return_freed_up_services_to_queue() {
        MockServiceCache cache = new MockServiceCache();
        for (Integer i = 1; i <= INSTANCE_LIMIT - 1; i++) {
            Integer service = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, mockVariant, mockConstructor);
            assertEquals(i, service);
            cache.queueFreedUpService(PUSH_MESSAGE_ID, mockVariant, service);
            service = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, mockVariant, mockConstructor);
            assertEquals(i, service);
        }
    }

    @Test
    public void returns_null_when_no_slots_available() {
        MockServiceCache cache = new MockServiceCache();
        for (Integer i = 1; i <= INSTANCE_LIMIT; i++) {
            assertEquals(i, cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, mockVariant, mockConstructor));
        }
        assertNull(cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, mockVariant, mockConstructor));
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
                        Integer service = cache.dequeueOrCreateNewService(PUSH_MESSAGE_ID, mockVariant, mockConstructor);
                        System.out.println(Thread.currentThread().getName() + ": #" + service);
                        sleep(INSTANTIATION_TIMEOUT / (threadCount * 2));
                        cache.queueFreedUpService(PUSH_MESSAGE_ID, mockVariant, service);
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
