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

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

/**
 * Receives a request for sending a push message to given variants from {@link NotificationRouter}.
 *
 * Loads device token batches from a database and queues them for processing inside a message holder.
 *
 * {@link TokenLoader} uses result stream with configured fetch size so that it can split database results into several batches.
 */
@Stateless
public class TokenLoader {

    private final static int NUMBER_OF_BATCHES = 10;
    private final static int BATCH_SIZE = 1000;

    private final AeroGearLogger logger = AeroGearLogger.getInstance(TokenLoader.class);

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    @DispatchToQueue
    private Event<MessageHolderWithTokens> dispatchTokensEvent;

    @Inject
    @DispatchToQueue
    private Event<MessageHolderWithVariants> nextBatchEvent;

    /**
     * Receives request for processing a {@link UnifiedPushMessage} and loads tokens for devices that match requested parameters from database.
     *
     * Device tokens are loaded in stream and split to batches of {@link #BATCH_SIZE}.
     * Once the pre-configured {@link #NUMBER_OF_BATCHES} is reached, this method resends message to the same queue it took the request from,
     * so that the transaction it worked in is split and further processing may continue in next transaction.
     */
    public void loadAndQueueTokenBatch(@Observes @Dequeue MessageHolderWithVariants msg) {
        final UnifiedPushMessage message = msg.getUnifiedPushMessage();
        final Collection<Variant> variants = msg.getVariants();
        final String lastTokenFromPreviousBatch = msg.getLastTokenFromPreviousBatch();
        logger.fine("Received message from queue: " + message.getMessage().getAlert());

        final Criteria criteria = message.getCriteria();
        final List<String> categories = criteria.getCategories();
        final List<String> aliases = criteria.getAliases();
        final List<String> deviceTypes = criteria.getDeviceTypes();

        for (Variant variant : variants) {
            ResultsStream<String> tokenStream =
                clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(variant.getVariantID(), categories, aliases, deviceTypes, NUMBER_OF_BATCHES * BATCH_SIZE, lastTokenFromPreviousBatch)
                                         .fetchSize(BATCH_SIZE)
                                         .executeQuery();

            try {
                String lastTokenInBatch = null;
                int tokensLoaded = 0;
                for (int batchNumber = 0; batchNumber < NUMBER_OF_BATCHES; batchNumber++) {
                    Set<String> tokens = new TreeSet<String>();
                    for (int i = 0; i < BATCH_SIZE && tokenStream.next(); i++) {
                        lastTokenInBatch = tokenStream.get();
                        tokens.add(lastTokenInBatch);
                        tokensLoaded += 1;
                    }
                    dispatchTokensEvent.fire(new MessageHolderWithTokens(msg.getPushMessageInformation(), message, variant, tokens));
                }
                // should we load next batch ?
                if (tokensLoaded >= NUMBER_OF_BATCHES * BATCH_SIZE) {
                    nextBatchEvent.fire(new MessageHolderWithVariants(msg.getPushMessageInformation(), message, msg.getVariantType(), variants, lastTokenInBatch));
                }
            } catch (ResultStreamException e) {
                logger.severe("Failed to load batch of tokens", e);
            }
        }
    }
}