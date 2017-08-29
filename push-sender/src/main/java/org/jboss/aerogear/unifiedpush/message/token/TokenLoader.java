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
package org.jboss.aerogear.unifiedpush.message.token;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import net.wessendorf.kafka.cdi.annotation.Consumer;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.dao.ResultStreamException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.message.Criteria;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.configuration.SenderConfiguration;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.message.kafka.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Receives a request for sending a push message to given variants via Kafka.
 *
 * Loads device token batches from a database and queues them for processing inside a message holder.
 *
 * {@link TokenLoader} uses result stream with configured fetch size so that it can split database results into several batches.
 */
@Stateless
public class TokenLoader {

    private final Logger logger = LoggerFactory.getLogger(TokenLoader.class);

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    @DispatchToQueue
    private Event<MessageHolderWithTokens> dispatchTokensEvent;

    @Inject
    @DispatchToQueue
    private Event<MessageHolderWithVariants> nextBatchEvent;

    @Inject @Any
    private Instance<SenderConfiguration> senderConfiguration;

    @Resource
    private EJBContext context;


    public final String ADM_TOPIC = "agpush_admPushMessageTopic";

    public final String ANDROID_TOPIC = "agpush_gcmPushMessageTopic";

    public final String IOS_TOPIC = "agpush_apnsPushMessageTopic";

    public final String SIMPLE_PUSH_TOPIC = "agpush_simplePushMessageTopic";

    public final String WINDOWS_MPNS_TOPIC = "agpush_mpnsPushMessageTopic";

    public final String WINDOWS_WNS_TOPIC = "agpush_wnsPushMessageTopic";


    /**
     * Consumes records from {@link org.jboss.aerogear.unifiedpush.kafka.streams.NotificationRouterStreamsHook} output topics
     * and loads tokens for devices that match requested parameters from database.
     *
     * Device tokens are loaded in a stream and split to batches of configured size (see {@link SenderConfiguration#batchSize()}).
     * Once the pre-configured number of batches (see {@link SenderConfiguration#batchesToLoad()}) is reached, this method resends message to the same queue it took the request from,
     * so that the transaction it worked in is split and further processing may continue in next transaction.
     *
     * @param msg holder object containing the payload and info about the effected variants
     */
    @Consumer(topics = {ADM_TOPIC, ADM_TOPIC, ANDROID_TOPIC, IOS_TOPIC, SIMPLE_PUSH_TOPIC, WINDOWS_MPNS_TOPIC, WINDOWS_WNS_TOPIC}, groupId = "agpush_tokenLoaderConsumerGroup")
    public void loadAndQueueTokenBatch(final MessageHolderWithVariants msg) throws IllegalStateException {
        final UnifiedPushMessage message = msg.getUnifiedPushMessage();
        final VariantType variantType = msg.getVariantType();
        final Collection<Variant> variants = msg.getVariants();
        final String lastTokenFromPreviousBatch = msg.getLastTokenFromPreviousBatch();
        final SenderConfiguration configuration = senderConfiguration.select(new SenderTypeLiteral(variantType)).get();
        int serialId = msg.getLastSerialId();

        logger.debug("Received message from queue: {}", message.getMessage().getAlert());

        final Criteria criteria = message.getCriteria();
        final List<String> categories = criteria.getCategories();
        final List<String> aliases = criteria.getAliases();
        final List<String> deviceTypes = criteria.getDeviceTypes();

        logger.info(String.format("Preparing message delivery and loading tokens for the %s 3rd-party Push Network (for %d variants)", variantType, variants.size()));

        for (Variant variant : variants) {

            try {

                ResultsStream<String> tokenStream;
                final Set<String> topics = new TreeSet<>();
                final boolean isAndroid = variantType == VariantType.ANDROID;

                // the entire batch size
                int batchesToLoad= configuration.batchesToLoad();

                // Some checks for GCM, because of GCM-3 topics
                boolean gcmTopicRequest = (isAndroid && TokenLoaderUtils.isGCMTopicRequest(criteria));
                if (gcmTopicRequest) {

                    // If we are able to do push for GCM topics...

                    // 1)
                    // find all topics, BUT only on the very first round of batches
                    // otherwise after 10 (or what ever the max. is) another request would be sent to that topic
                    if (serialId == 0) {
                        topics.addAll(TokenLoaderUtils.extractGCMTopics(criteria, variant.getVariantID()));

                        // topics are handled as a first extra batch,
                        // therefore we have to adjust the number by adding this extra batch
                        batchesToLoad += 1;
                    }

                    // 2) always load the legacy tokens, for all number of batch iterations
                    tokenStream = clientInstallationService.findAllOldGoogleCloudMessagingDeviceTokenForVariantIDByCriteria(variant.getVariantID(), categories, aliases, deviceTypes, configuration.tokensToLoad(), lastTokenFromPreviousBatch)
                            .fetchSize(configuration.batchSize())
                            .executeQuery();
                } else {
                    tokenStream = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(variant.getVariantID(), categories, aliases, deviceTypes, configuration.tokensToLoad(), lastTokenFromPreviousBatch)
                            .fetchSize(configuration.batchSize())
                            .executeQuery();
                }

                String lastTokenInBatch = null;
                int tokensLoaded = 0;
                for (int batchNumber = 0; batchNumber < batchesToLoad; batchNumber++) {

                    // increasing the serial ID,
                    // to make sure it's properly read from all block
                    ++serialId;

                    final Set<String> tokens = new TreeSet<>();

                    // On Android, the first batch is for GCM3 topics
                    // legacy tokens are submitted in the batch #2 and later
                    if (isAndroid && batchNumber == 0 && ! topics.isEmpty()) {
                        tokens.addAll(topics);
                    } else {
                        for (int i = 0; i < configuration.batchSize() && tokenStream.next(); i++) {
                            lastTokenInBatch = tokenStream.get();
                            tokens.add(lastTokenInBatch);
                            tokensLoaded += 1;
                        }
                    }

                    if (tokens.size() > 0) {
                        if (tryToDispatchTokens(new MessageHolderWithTokens(msg.getPushMessageInformation(), message, variant, tokens, serialId))) {
                            logger.info(String.format("Loaded batch #%s, containing %d tokens, for %s variant (%s)", serialId, tokens.size() ,variant.getType().getTypeName(), variant.getVariantID()));
                        } else {
                            logger.debug(String.format("Failing token loading transaction for batch token #%s for %s variant (%s), since queue is full, will retry...", serialId, variant.getType().getTypeName(), variant.getVariantID()));
                            context.setRollbackOnly();
                            return;
                        }
                        logger.info("Loaded batch #{}, containing {} tokens, for {} variant ({})", serialId, tokens.size() ,variant.getType().getTypeName(), variant.getVariantID());

                    } else {
                        logger.debug("Ending batch processing: No more tokens for batch #{} available", serialId);
                        break;
                    }
                }
                // should we trigger next transaction batch ?
                if (tokensLoaded >= configuration.tokensToLoad()) {
                    logger.debug(String.format("Ending token loading transaction for %s variant (%s)", variant.getType().getTypeName(), variant.getVariantID()));
                    nextBatchEvent.fire(new MessageHolderWithVariants(msg.getPushMessageInformation(), message, msg.getVariantType(), variants, serialId, lastTokenInBatch));
                } else {
                    logger.debug("All batches for {} variant were loaded ({})", variant.getType().getTypeName(), variant.getVariantID());

                    if (tokensLoaded == 0 && lastTokenFromPreviousBatch == null) {
                        // no tokens were loaded at all!
                        if (gcmTopicRequest) {
                            logger.debug("No legacy(non-InstanceID) tokens found. Just pure GCM topic requests");
                        } else {
                            logger.warn("Check your push query: Not a single token was loaded from the DB!");
                        }
                    }
                }
            } catch (ResultStreamException e) {
                logger.error("Failed to load batch of tokens", e);
            }
        }
    }

    /**
     * Tries to dispatch tokens; returns true if tokens were successfully queued.
     * Detects when queue is full and in that case returns false.
     *
     * @return returns true if tokens were successfully queued; returns false if queue was full
     */
    private boolean tryToDispatchTokens(MessageHolderWithTokens msg) {
        try {
            dispatchTokensEvent.fire(msg);
            return true;
        } catch (Exception e) {
            logger.error("Failed to load batch of tokens", e);
            throw e;
        }
    }

}
