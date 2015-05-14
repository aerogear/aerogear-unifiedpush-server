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

import javax.enterprise.context.ApplicationScoped;

import com.notnoop.apns.ApnsService;

/**
 * This cache creates and holds queue of used {@link ApnsService} with upper-bound limit of 15 created instances
 * per given push message and variant.
 *
 * Cache allows to return freed up services to the queue or free a slot for creating new services up to the limit.
 *
 * @see AbstractServiceCache#dequeueOrCreateNewService(String, org.jboss.aerogear.unifiedpush.api.Variant, org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache.ServiceConstructor)
 * @see AbstractServiceCache#queueFreedUpService(String, org.jboss.aerogear.unifiedpush.api.Variant, Object)
 * @see AbstractServiceCache#freeUpSlot(String, org.jboss.aerogear.unifiedpush.api.Variant)
 */
@ApplicationScoped
public class ApnsServiceCache extends AbstractServiceCache<ApnsService> {

    public static final int INSTANCE_LIMIT = 15;
    public static final long INSTANCE_ACQUIRING_TIMEOUT = 5000;

    public ApnsServiceCache() {
        super(INSTANCE_LIMIT, INSTANCE_ACQUIRING_TIMEOUT);
    }
}
