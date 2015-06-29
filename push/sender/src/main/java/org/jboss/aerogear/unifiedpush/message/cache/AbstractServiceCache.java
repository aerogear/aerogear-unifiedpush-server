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
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.jms.Queue;

import org.jboss.aerogear.unifiedpush.message.jms.util.JMSExecutor;

/**
 * Abstract cache holds queue of services with upper-bound limit of created instances.
 *
 * The limit is enforced across entire cluster of servers by maintaining a queue as a counter of available slots.
 *
 * Cache allows to return freed up services to the queue or free a slot for creating new services up to a limit.
 */
public abstract class AbstractServiceCache<T> {

    private final ConcurrentHashMap<Key, ConcurrentLinkedQueue<DisposableReference<T>>> queueMap = new ConcurrentHashMap<Key, ConcurrentLinkedQueue<DisposableReference<T>>>();

    private final int instanceLimit;
    private final long instanceAcquiringTimeoutInMillis;
    private final long serviceDisposalDelayInMillis;

    @Inject
    private JMSExecutor jmsExecutor;

    @Inject
    private ServiceDisposalScheduler serviceDisposalScheduler;

    /**
     * Creates new cache
     *
     * @param instanceLimit how many instances can be created
     * @param instanceAcquiringTimeoutInMillis what is a timeout before the cache can return null
     */
    public AbstractServiceCache(int instanceLimit, long instanceAcquiringTimeoutInMillis, long serviceDisposalDelayInMillis) {
        this.instanceLimit = instanceLimit;
        this.instanceAcquiringTimeoutInMillis = instanceAcquiringTimeoutInMillis;
        this.serviceDisposalDelayInMillis = serviceDisposalDelayInMillis;
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
     * The service blocks until a service is available or configured {@link #instanceAcquiringTimeoutInMillis}.
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
        ConcurrentLinkedQueue<DisposableReference<T>> concurrentLinkedQueue = getCache(pushMessageInformationId, variantID);
        DisposableReference<T> serviceHolder;
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
    public void queueFreedUpService(final String pushMessageInformationId, final String variantID, final T service, final ServiceDestroyer<T> destroyer) {
        ServiceDestroyer<T> destroyAndReturnBadge = new ServiceDestroyer<T>() {
            @Override
            public void destroy(T instance) {
                destroyer.destroy(instance);
                returnBadge(pushMessageInformationId);
            };
        };
        DisposableReference<T> disposableReference = new DisposableReference<T>(service, destroyAndReturnBadge);
        serviceDisposalScheduler.scheduleForDisposal(disposableReference, serviceDisposalDelayInMillis);
        getCache(pushMessageInformationId, variantID).add(disposableReference);
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

    protected Object leaseBadge(String pushMessageInformationId) {
        return jmsExecutor.receiveWithSelectorAndWait(getBadgeQueue(), String.format("pushMessageInformationId = '%s'", pushMessageInformationId), instanceAcquiringTimeoutInMillis);
    }

    protected void returnBadge(String pushMessageInformationId) {
        jmsExecutor.sendWithProperty(getBadgeQueue(), pushMessageInformationId, String.format("pushMessageInformationId=%s", pushMessageInformationId));
    }

    private ConcurrentLinkedQueue<DisposableReference<T>> getCache(String pushMessageInformationId, String variantID) {
        return getOrCreateQueue(new Key(pushMessageInformationId, variantID));
    }

    private ConcurrentLinkedQueue<DisposableReference<T>> getOrCreateQueue(Key key) {
        ConcurrentLinkedQueue<DisposableReference<T>> queue = queueMap.get(key);
        if (queue == null) {
            queue = queueMap.putIfAbsent(key, new ConcurrentLinkedQueue<DisposableReference<T>>());
            queue = queueMap.get(key);
        }
        return queue;
    }

    /**
     * The key that is used to store a queue instance in the map.
     */
    private static class Key {

        private String pushMessageInformationId;
        private String variantId;

        Key (String pushMessageInformationId, String variantID) {
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
            Key other = (Key) obj;
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
