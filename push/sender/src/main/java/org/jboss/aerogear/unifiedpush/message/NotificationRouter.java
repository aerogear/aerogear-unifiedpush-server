/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Takes a request for sending {@link UnifiedPushMessage} and submits it to messaging subsystem for further processing.
 *
 * Router splits messages to specific variant types (push network type) so that they can be processed separately,
 * giving attention to limitations and requirements of specific push networks.
 *
 * {@link NotificationRouter} receives a request for sending a {@link UnifiedPushMessage} and queues one message per variant type, both in transaction.
 * The transactional behavior makes sure the request for sending notification is recorded and then asynchronously processed.
 *
 * The further processing of the push message happens in {@link TokenLoader}.
 */
@Stateless
public class NotificationRouter {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(NotificationRouter.class);

    @Inject
    private GenericVariantService genericVariantService;
    @Inject
    private PushMessageMetricsService metricsService;

    @Inject
    @DispatchToQueue
    private Event<MessageHolderWithVariants> dispatchVariantMessageEvent;

    /**
     * Receives a request for sending a {@link UnifiedPushMessage} and queues one message per variant type, both in one transaction.
     *
     * Once this method returns, message is recorded and will be eventually delivered in the future.
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void submit(PushApplication pushApplication, UnifiedPushMessage message) {
        logger.info("Processing send request with '" + message.toString() + "' payload");

        final PushMessageInformation pushMessageInformation =
                metricsService.storeNewRequestFrom(
                        pushApplication.getPushApplicationID(),
                        message.toStrippedJsonString(),
                        message.getIpAddress(),
                        message.getClientIdentifier()
                        );

        // collections for all the different variants:
        final VariantMap variants = new VariantMap();

        final List<String> variantIDs = message.getCriteria().getVariants();

        // if the criteria payload did specify the "variants" field,
        // we look up each of those mentioned variants, by their "variantID":
        if (variantIDs != null) {

            for (String variantID : variantIDs) {
                Variant variant = genericVariantService.findByVariantID(variantID);

                // does the variant exist ?
                if (variant != null) {
                    variants.add(variant);
                }
            }
        } else {
            // No specific variants have been requested,
            // we get all the variants, from the given PushApplicationEntity:
            variants.addAll(pushApplication.getVariants());
        }


        // we split the variants per type since each type may have its own configuration (e.g. batch size)
        for (final Entry<VariantType, List<Variant>> entry : variants.entrySet()) {
            dispatchVariantMessageEvent.fire(new MessageHolderWithVariants(pushMessageInformation, message, entry.getKey(), entry.getValue()));
        }
    }

    /**
     * Map for storing variants split by the variant type
     */
    private static class VariantMap extends EnumMap<VariantType, List<Variant>> {
        private static final long serialVersionUID = -1942168038908630961L;
        VariantMap() {
            super(VariantType.class);
        }
        void add(Variant variant) {
            List<Variant> list = this.get(variant.getType());
            if (list == null) {
                list = new ArrayList<Variant>();
                this.put(variant.getType(), list);
            }
            list.add(variant);
        }
        void addAll(Collection<Variant> variants) {
            for (Variant variant : variants) {
                this.add(variant);
            }
        }
    }
}
