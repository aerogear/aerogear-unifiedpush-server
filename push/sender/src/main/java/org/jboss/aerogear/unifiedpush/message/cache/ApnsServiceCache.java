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
import javax.enterprise.event.Observes;

import org.jboss.aerogear.unifiedpush.message.holder.VariantCompleted;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import com.notnoop.apns.ApnsService;

/**
 * This cache creates and holds queue of used {@link ApnsService} with upper-bound limit of 15 created instances
 * per given push message and variant.
 *
 * Cache allows to return freed up services to the queue or free a slot for creating new services up to the limit.
 *
 * This cache also listens for {@link VariantCompleted} event and stops all instantiated {@link ApnsService}s and frees the cache.
 *
 * @see AbstractServiceCache#dequeueOrCreateNewService(String, org.jboss.aerogear.unifiedpush.api.Variant, org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache.ServiceConstructor)
 * @see AbstractServiceCache#queueFreedUpService(String, org.jboss.aerogear.unifiedpush.api.Variant, Object)
 * @see AbstractServiceCache#freeUpSlot(String, org.jboss.aerogear.unifiedpush.api.Variant)
 */
@ApplicationScoped
public class ApnsServiceCache extends AbstractServiceCache<ApnsService> {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(ApnsServiceCache.class);

    public static final int INSTANCE_LIMIT = 15;
    public static final long INSTANCE_ACQUIRING_TIMEOUT = 5000;

    public ApnsServiceCache() {
        super(INSTANCE_LIMIT, INSTANCE_ACQUIRING_TIMEOUT);
    }

    public void freeUpAvailableServices(@Observes VariantCompleted variantCompleted) {
        final String pushMessageInformationId = variantCompleted.getPushMessageInformationId();
        String variantID = variantCompleted.getVariantID();

        ApnsService service;
        while ((service = this.dequeue(pushMessageInformationId, variantID)) != null) {
            try {
                service.stop();
            } catch (Exception e) {
                logger.severe("Unable to stop ApnsService", e);
            } finally {
                this.freeUpSlot(pushMessageInformationId, variantID);
            }
        }
    }
}
