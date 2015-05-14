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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract cache holds queue of services with upper-bound limit of created instances.
 *
 * Cache allows to return freed up services to the queue or free a slot for creating new services up to a limit.
 */
public abstract class AbstractServiceCache<T> {

    private static final long QUEUE_POLLING_INTERVAL_IN_MILLIS = 100;

    private final ConcurrentHashMap<InstanceKey, Holder> holderMap = new ConcurrentHashMap<InstanceKey, Holder>();

    private final int instanceLimit;
    private final long timeout;

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
     */
    public T dequeueOrCreateNewService(final String pushMessageInformationId, final String variantID, ServiceConstructor<T> constructor) {
        Holder holder = getOrCreateHolder(new InstanceKey(pushMessageInformationId, variantID));
        T service = holder.dequeueOrCreateBlocking(constructor, timeout);
        return service;
    }

    /**
     * Dequeues the service instance if there is one available, otherwise returns null
     * @param pushMessageInformationId the push message id
     * @param variant the variant
     * @return the service instance or null if no instance is queued
     */
    public T dequeue(final String pushMessageInformationId, final String variantID) {
        Holder holder = getHolder(new InstanceKey(pushMessageInformationId, variantID));
        if (holder == null) {
            return null;
        }
        return holder.dequeue();
    }

    /**
     * Allows to queue used and freed up service into cache so that can be reused by another consumer.
     *
     * @param pushMessageInformationId the push message
     * @param variant the variant
     * @param service the used and freed up service
     */
    public void queueFreedUpService(final String pushMessageInformationId, final String variantID, T service) {
        Holder holder = getOrCreateHolder(new InstanceKey(pushMessageInformationId, variantID));
        holder.queue(service);
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
        InstanceKey instanceKey = new InstanceKey(pushMessageInformationId, variantID);
        Holder holder = getOrCreateHolder(instanceKey);
        int newInstanceCount = holder.decrementCounter();
        if (newInstanceCount == 0) {
            freeUpHolder(instanceKey, holder);
        } else if (newInstanceCount < 0) {
            throw new IllegalStateException("Instance counter cant be less than zero");
        }
    }

    private Holder getHolder(InstanceKey key) {
        return holderMap.get(key);
    }

    private Holder getOrCreateHolder(InstanceKey key) {
        Holder holder = holderMap.get(key);
        if (holder == null) {
            holder = holderMap.putIfAbsent(key, new Holder());
            holder = holderMap.get(key);
        }
        return holder;
    }

    private void freeUpHolder(InstanceKey key, Holder holder) {
        holderMap.remove(key, holder);
    }

    public static interface ServiceConstructor<T> {
        T construct();
    }

    private class Holder {
        private ConcurrentLinkedQueue<T> queue = new ConcurrentLinkedQueue<T>();
        private AtomicInteger counter = new AtomicInteger(0);

        public T dequeueOrCreateBlocking(ServiceConstructor<T> constructor, long timeoutInMillis) {
            for (long start = System.currentTimeMillis(); start + timeoutInMillis > System.currentTimeMillis(); ) {
                T service = dequeueOrCreate(constructor);
                if (service != null) {
                    return service;
                }
                try {
                    Thread.sleep(QUEUE_POLLING_INTERVAL_IN_MILLIS);
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }

        public void queue(T service) {
            queue.add(service);
        }

        public T dequeue() {
            return queue.poll();
        }

        public int decrementCounter() {
            return counter.decrementAndGet();
        }

        private T dequeueOrCreate(ServiceConstructor<T> constructor) {
            // try to use existing queued instance
            if (!queue.isEmpty()) {
                return queue.poll();
            }
            int count = counter.get();
            // create new instance
            if (count < instanceLimit) {
                if (counter.compareAndSet(count, count + 1)) {
                    T service = null;
                    try {
                        service = constructor.construct();
                    } finally {
                        if (service == null) {
                            // service construction failed, we need to free up a slot
                            counter.decrementAndGet();
                        }
                    }
                    return service;
                }
            }
            return null;
        }
    }

    private static class InstanceKey {

        private String pushMessageInformationId;
        private String variantId;

        InstanceKey (String pushMessageInformationId, String variantID) {
            if (pushMessageInformationId == null) {
                throw new NullPointerException("pushMessageInformationId");
            }
            if (variantID == null) {
                throw new NullPointerException("variant or its variantID cant be null");
            }
            this.pushMessageInformationId = pushMessageInformationId;
            this.variantId = variantID;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((pushMessageInformationId == null) ? 0 : pushMessageInformationId.hashCode());
            result = prime * result + ((variantId == null) ? 0 : variantId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            InstanceKey other = (InstanceKey) obj;
            if (pushMessageInformationId == null) {
                if (other.pushMessageInformationId != null)
                    return false;
            } else if (!pushMessageInformationId.equals(other.pushMessageInformationId))
                return false;
            if (variantId == null) {
                if (other.variantId != null)
                    return false;
            } else if (!variantId.equals(other.variantId))
                return false;
            return true;
        }
    }
}
