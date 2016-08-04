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
package org.jboss.aerogear.unifiedpush.message.serviceHolder;

import com.notnoop.apns.ApnsService;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.event.VariantCompletedEvent;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Queue;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * This cache creates and holds queue of used {@link ApnsService} with upper-bound limit of 10 created instances
 * per given push message and variant. This allows for 10 concurrent connections to the APNs push network.
 *
 * Cache allows to return freed up services to the queue or free a slot for creating new services up to the limit.
 *
 * This cache also listens for {@link VariantCompletedEvent} event and stops all instantiated {@link ApnsService}s and frees the cache.
 *
 * @see AbstractServiceHolder#dequeueOrCreateNewService(String, String, ServiceConstructor)
 * @see AbstractServiceHolder#queueFreedUpService(String, String, Object, ServiceDestroyer)
 * @see AbstractServiceHolder#freeUpSlot(String, String)
 */
@ApplicationScoped
public class ApnsServiceHolder extends AbstractServiceHolder<ApnsService> {

    private final Logger logger = LoggerFactory.getLogger(ApnsServiceHolder.class);

    public static final int INSTANCE_LIMIT = 10;
    public static final long INSTANCE_ACQUIRING_TIMEOUT = 7500;
    public static final long DISPOSING_DELAY = 5000;

    @Inject
    private ClientInstallationService clientInstallationService;

    @Resource(mappedName = "java:/queue/FreeServiceSlotQueue")
    private Queue freeServiceCounterQueue;

    public ApnsServiceHolder() {
        super(INSTANCE_LIMIT, INSTANCE_ACQUIRING_TIMEOUT, DISPOSING_DELAY);
    }

    @Override
    public Queue getFreeServiceSlotQueue() {
        return freeServiceCounterQueue;
    }

    public void initializeHolderForVariants(@Observes MessageHolderWithVariants msg) throws ExecutionException {
        if (msg.getVariantType() == VariantType.IOS) {
            msg.getVariants().forEach(variant -> initialize(msg.getPushMessageInformation().getId(), variant.getVariantID()));
        }
    }

    /**
     * Listen to {@link VariantCompletedEvent} to free up APNs bound resources after the (iOS) variant has
     * been completely processed for push notification delivery.
     */
    public void freeUpAvailableServices(@Observes VariantCompletedEvent variantCompleted) throws ExecutionException {
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
                        logger.info("Deleting '{}' inactive iOS installations", inactiveTokens.size());
                        clientInstallationService.removeInstallationsForVariantByDeviceTokens(variantID, transformedTokens);
                    }
                } catch (Exception e) {
                    logger.error("Unable to detect and delete inactive devices", e);
                }
                // kill the service
                service.stop();
            } catch (Exception e) {
                logger.error("Unable to stop ApnsService", e);
            } finally {
                // we will free up a slot anyway
                this.freeUpSlot(pushMessageInformationId, variantID);
            }
        }

        this.destroy(pushMessageInformationId, variantID);
    }

    /**
     * The Java-APNs lib returns the tokens in UPPERCASE format, however, the iOS Devices submit the token in
     * LOWER CASE format. This helper method performs a transformation
     */
    private static Set<String> lowerCaseAllTokens(Set<String> inactiveTokens) {
        return inactiveTokens.stream().map(String::toLowerCase).collect(Collectors.toSet());
    }
}
