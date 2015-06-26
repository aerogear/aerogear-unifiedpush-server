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
package org.jboss.aerogear.unifiedpush.message.cache;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.jms.util.JMSExecutor;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

/**
 * Abstract cache holds queue of services with upper-bound limit of created instances.
 *
 * Cache allows to return freed up services to the queue or free a slot for creating new services up to a limit.
 */
public abstract class AbstractServiceCache<T> {

    private LoadingCache<String, ConcurrentLinkedQueue<ServiceHolder>> cache = CacheBuilder.newBuilder()
            .build(
                new CacheLoader<String, ConcurrentLinkedQueue<ServiceHolder>>() {
                  @Override
                    public ConcurrentLinkedQueue<ServiceHolder> load(String key) throws Exception {
                        return new ConcurrentLinkedQueue<ServiceHolder>();
                    }
                });

    private final int instanceLimit;
    private final long timeout;

    @Inject
    private JMSExecutor jmsExecutor;

    /**
     * Creates new cache
     *
     * @param instanceLimit how many instances can be created
     * @param instanceAcquiringTimeoutInMillis what is a timeout before the cache can return null
     */
    public AbstractServiceCache(int instanceLimit, long instanceAcquiringTimeoutInMillis) {
        this.instanceLimit = instanceLimit;
        this.timeout = instanceAcquiringTimeoutInMillis;
    }

    public abstract Queue getBadgeQueue();

    public void initialize(final String pushMessageInformationId, final String variantID) {
        for (int i = 0; i < instanceLimit; i++) {
            returnBadge(pushMessageInformationId);
        }
    }

    public void destroy(final String pushMessageInformationId, final String variantID) {
        for (int i = 0; i < instanceLimit; i++) {
            if (leaseBadge(pushMessageInformationId) == null) {
                return;
            }
        }
    }

    /**
     * Cache returns a service for given parameters or uses service constructor to instantiate new service.
     *
     * Number of created or queued services is limited up to configured {@link #instanceLimit}.
     *
     * The service blocks until a service is available or configured {@link #timeout}.
     *
     * In case the service is not available when times out, cache returns null.
     *
     * @param pushMessageInformationId the push message id
     * @param variant the variant
     * @param constructor the service constructor
     * @return the service instance; or null in case too much services were created and no services are queued for reuse
     * @throws ExecutionException
     */
    public T dequeueOrCreateNewService(final String pushMessageInformationId, final String variantID, ServiceConstructor<T> constructor) {
        T instance = dequeue(pushMessageInformationId, variantID);
        if (instance != null) {
            return instance;
        }
        // there is no cached instance, try to establish one
        if (leaseBadge(pushMessageInformationId) != null) {
            // we have leased a badge, we can create new instance
            return constructor.construct();
        }
        return null;
    }

    /**
     * Dequeues the service instance if there is one available, otherwise returns null
     * @param pushMessageInformationId the push message id
     * @param variant the variant
     * @return the service instance or null if no instance is queued
     * @throws ExecutionException
     */
    public T dequeue(final String pushMessageInformationId, final String variantID) {
        ConcurrentLinkedQueue<ServiceHolder> concurrentLinkedQueue = getCache(pushMessageInformationId);
        ServiceHolder serviceHolder;
        // poll queue for new instance
        while ((serviceHolder = concurrentLinkedQueue.poll()) != null) {
            T serviceInstance = serviceHolder.get();
            // holder may hold expired instance
            if (serviceInstance != null) {
                return serviceInstance;
            }
        }
        return null;
    }

    /**
     * Allows to queue used and freed up service into cache so that can be reused by another consumer.
     *
     * @param pushMessageInformationId the push message
     * @param variant the variant
     * @param service the used and freed up service
     * @throws ExecutionException
     */
    public void queueFreedUpService(final String pushMessageInformationId, final String variantID, T service, ServiceDestroyer<T> destroyer) {
        getCache(pushMessageInformationId).add(new ServiceHolder(pushMessageInformationId, service, destroyer));
    }

    /**
     * Allows to free up a counter of created services and thus allowing waiting consumers to create new services within the limits.
     *
     * Freed up service is a service that died, disconnected or similar and can no longer be used.
     *
     * @param pushMessageInformationId the push message
     * @param variant the variant
     */
    public void freeUpSlot(final String pushMessageInformationId, final String variantID) {
        returnBadge(pushMessageInformationId);
    }

    public static interface ServiceConstructor<T> {
        T construct();
    }

    public static interface ServiceDestroyer<T> {
        void destroy(T instance);
    }

    private class ServiceHolder {

        private LoadingCache<Class<Void>, T> holder;

        public ServiceHolder(final String pushMessageInformationId, T instance, final ServiceDestroyer<T> destroyer) {
            final AtomicReference<T> reference = new AtomicReference<T>(instance);
            holder = CacheBuilder.newBuilder()
                    .initialCapacity(1).maximumSize(1)
                    .expireAfterWrite(5000, TimeUnit.MILLISECONDS)
                    .removalListener(new RemovalListener<Class<Void>, T>() {
                        @Override
                        public void onRemoval(RemovalNotification<Class<Void>, T> notification) {
                            T instance = reference.getAndSet(null);
                            destroyer.destroy(instance);
                            returnBadge(pushMessageInformationId);
                        }
                    })
                    .build(CacheLoader.from(new Supplier() {
                        @Override
                        public Object get() {
                            return reference.get();
                        }
                    }));
        }

        public T get() {
            try {
                return this.holder.get(Void.class);
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    protected Object leaseBadge(String pushMessageInformationId) {
        return jmsExecutor.receive(getBadgeQueue(), String.format("pushMessageInformationId = '%s'", pushMessageInformationId), timeout);
    }

    protected void returnBadge(String pushMessageInformationId) {
        jmsExecutor.send(getBadgeQueue(), pushMessageInformationId, String.format("pushMessageInformationId=%s", pushMessageInformationId));
    }

    private ConcurrentLinkedQueue<ServiceHolder> getCache(String pushMessageInformationId) {
        try {
            return cache.get(pushMessageInformationId);
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
