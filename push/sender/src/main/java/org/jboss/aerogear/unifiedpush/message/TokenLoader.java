/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.message.configuration.SenderConfiguration;
import org.jboss.aerogear.unifiedpush.message.event.AllBatchesLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.event.BatchLoadedEvent;
import org.jboss.aerogear.unifiedpush.message.event.TriggerMetricCollection;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Receives a request for sending a push message to given variants from {@link NotificationRouter}.
 *
 * Loads device token batches from a database and queues them for processing inside a message holder.
 *
 * {@link TokenLoader} uses result stream with configured fetch size so that it can split database results into several batches.
 */
@Stateless
public class TokenLoader {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(TokenLoader.class);

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    @DispatchToQueue
    private Event<MessageHolderWithTokens> dispatchTokensEvent;

    @Inject
    @DispatchToQueue
    private Event<MessageHolderWithVariants> nextBatchEvent;

    @Inject
    @DispatchToQueue
    private Event<BatchLoadedEvent> batchLoaded;

    @Inject
    @DispatchToQueue
    private Event<AllBatchesLoadedEvent> allBatchesLoaded;

    @Inject
    @DispatchToQueue
    private Event<TriggerMetricCollection> triggerMetricCollection;

    @Inject
    @DispatchToQueue
    private Event<VariantMetricInformation> dispatchVariantMetricEvent;

    @Inject @Any
    private Instance<SenderConfiguration> senderConfiguration;

    /**
     * Receives request for processing a {@link UnifiedPushMessage} and loads tokens for devices that match requested parameters from database.
     *
     * Device tokens are loaded in a stream and split to batches of configured size (see {@link SenderConfiguration#batchSize()}).
     * Once the pre-configured number of batches (see {@link SenderConfiguration#batchesToLoad()}) is reached, this method resends message to the same queue it took the request from,
     * so that the transaction it worked in is split and further processing may continue in next transaction.
     *
     * Additionally it fires {@link BatchLoadedEvent} as CDI event (that is translated to JMS event) that helps {@link MetricsCollector} to track how many batches were loaded.
     * When all batches were loaded for the given variant, it fires  {@link AllBatchesLoadedEvent}.
     *
     * @param msg holder object containing the payload and info about the effected variants
     */
    public void loadAndQueueTokenBatch(@Observes @Dequeue MessageHolderWithVariants msg) {
        final UnifiedPushMessage message = msg.getUnifiedPushMessage();
        final VariantType variantType = msg.getVariantType();
        final Collection<Variant> variants = msg.getVariants();
        final String lastTokenFromPreviousBatch = msg.getLastTokenFromPreviousBatch();
        final SenderConfiguration configuration = senderConfiguration.select(new SenderTypeLiteral(variantType)).get();
        int serialId = msg.getLastSerialId();

        logger.fine("Received message from queue: " + message.getMessage().getAlert());

        final Criteria criteria = message.getCriteria();
        final List<String> categories = criteria.getCategories();
        final List<String> aliases = criteria.getAliases();
        final List<String> deviceTypes = criteria.getDeviceTypes();

        logger.info(String.format("Preparing message delivery and loading tokens for the %s 3rd-party Push Network (for %d variants)", variantType, variants.size()));
        for (Variant variant : variants) {
            ResultsStream<String> tokenStream =
                clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(variant.getVariantID(), categories, aliases, deviceTypes, configuration.tokensToLoad(), lastTokenFromPreviousBatch)
                                         .fetchSize(configuration.batchSize())
                                         .executeQuery();

            try {
                String lastTokenInBatch = null;
                int tokensLoaded = 0;
                for (int batchNumber = 0; batchNumber < configuration.batchesToLoad(); batchNumber++) {
                    Set<String> tokens = new TreeSet<String>();
                    for (int i = 0; i < configuration.batchSize() && tokenStream.next(); i++) {
                        lastTokenInBatch = tokenStream.get();
                        tokens.add(lastTokenInBatch);
                        tokensLoaded += 1;
                    }
                    if (tokens.size() > 0) {
                        dispatchTokensEvent.fire(new MessageHolderWithTokens(msg.getPushMessageInformation(), message, variant, tokens, ++serialId));
                        logger.info(String.format("Loaded batch #%s, containing %d tokens, for %s variant (%s)", serialId, tokens.size() ,variant.getType().getTypeName(), variant.getVariantID()));

                        // using combined key of variant and PMI (AGPUSH-1585):
                        batchLoaded.fire(new BatchLoadedEvent(variant.getVariantID()+":"+msg.getPushMessageInformation().getId()));
                        triggerMetricCollection.fire(new TriggerMetricCollection(msg.getPushMessageInformation()));
                    } else {
                        break;
                    }
                }
                // should we load next batch ?
                if (tokensLoaded >= configuration.tokensToLoad()) {
                    logger.fine(String.format("Ending token loading transaction for %s variant (%s)", variant.getType().getTypeName(), variant.getVariantID()));
                    nextBatchEvent.fire(new MessageHolderWithVariants(msg.getPushMessageInformation(), message, msg.getVariantType(), variants, serialId, lastTokenInBatch));
                } else {
                    logger.fine(String.format("All batches for %s variant were loaded (%s)", variant.getType().getTypeName(), msg.getPushMessageInformation().getId()));

                    // using combined key of variant and PMI (AGPUSH-1585):
                    allBatchesLoaded.fire(new AllBatchesLoadedEvent(variant.getVariantID()+":"+msg.getPushMessageInformation().getId()));
                    triggerMetricCollection.fire(new TriggerMetricCollection(msg.getPushMessageInformation()));

                    if (tokensLoaded == 0 && lastTokenFromPreviousBatch == null) {
                        // no tokens were loaded at all!
                        VariantMetricInformation variantMetricInformation = new VariantMetricInformation();
                        variantMetricInformation.setPushMessageInformation(msg.getPushMessageInformation());
                        variantMetricInformation.setVariantID(variant.getVariantID());
                        variantMetricInformation.setDeliveryStatus(Boolean.TRUE);
                        dispatchVariantMetricEvent.fire(variantMetricInformation);
                    }
                }
            } catch (ResultStreamException e) {
                logger.severe("Failed to load batch of tokens", e);
            }
        }
    }
}