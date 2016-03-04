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

import com.notnoop.apns.ApnsService;
import org.jboss.aerogear.unifiedpush.message.event.VariantCompletedEvent;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * This cache creates and holds queue of used {@link ApnsService} with upper-bound limit of 10 created instances
 * per given push message and variant. This allows for 10 concurrent connections to the APNs push network.
 *
 * Cache allows to return freed up services to the queue or free a slot for creating new services up to the limit.
 *
 * This cache also listens for {@link VariantCompletedEvent} event and stops all instantiated {@link ApnsService}s and frees the cache.
 *
 * @see AbstractServiceCache#dequeueOrCreateNewService(String, String, ServiceConstructor)
 * @see AbstractServiceCache#queueFreedUpService(String, String, Object)
 * @see AbstractServiceCache#freeUpSlot(String, String)
 */
@ApplicationScoped
public class ApnsServiceCache extends AbstractServiceCache<ApnsService> {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(ApnsServiceCache.class);

    public static final int INSTANCE_LIMIT = 10;
    public static final long INSTANCE_ACQUIRING_TIMEOUT = 5000;

    @Inject
    private ClientInstallationService clientInstallationService;

    public ApnsServiceCache() {
        super(INSTANCE_LIMIT, INSTANCE_ACQUIRING_TIMEOUT);
    }

    public void freeUpAvailableServices(@Observes VariantCompletedEvent variantCompleted) {
        final String pushMessageInformationId = variantCompleted.getPushMessageInformationId();
        String variantID = variantCompleted.getVariantID();

        ApnsService service;
        while ((service = this.dequeue(pushMessageInformationId, variantID)) != null) {
            try {
                try {
                    // after sending, let's ask for the inactive tokens:
                    final Set<String> inactiveTokens = service.getInactiveDevices().keySet();
                    // transform the tokens to be all lower-case:
                    final Set<String> transformedTokens = lowerCaseAllTokens(inactiveTokens);

                    // trigger asynchronous deletion:
                    if (! transformedTokens.isEmpty()) {
                        logger.info("Deleting '" + inactiveTokens.size() + "' invalid iOS installations");
                        clientInstallationService.removeInstallationsForVariantByDeviceTokens(variantID, transformedTokens);
                    }
                } catch (Exception e) {
                    logger.severe("Unable to detect and delete inactive devices", e);
                }
                // kill the service
                service.stop();
            } catch (Exception e) {
                logger.severe("Unable to stop ApnsService", e);
            } finally {
                // we will free up a slot anyway
                this.freeUpSlot(pushMessageInformationId, variantID);
            }
        }
    }

    /**
     * The Java-APNs lib returns the tokens in UPPERCASE format, however, the iOS Devices submit the token in
     * LOWER CASE format. This helper method performs a transformation
     */
    private Set<String> lowerCaseAllTokens(Set<String> inactiveTokens) {
        final Set<String> lowerCaseTokens = new HashSet<String>();
        for (String token : inactiveTokens) {
            lowerCaseTokens.add(token.toLowerCase());
        }
        return lowerCaseTokens;
    }
}
