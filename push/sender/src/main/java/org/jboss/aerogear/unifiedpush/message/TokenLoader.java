package org.jboss.aerogear.unifiedpush.message;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.BatchException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithTokens;
import org.jboss.aerogear.unifiedpush.message.holder.MessageHolderWithVariants;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

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

    public void loadAndQueueTokenBatch(@Observes @Dequeue MessageHolderWithVariants msg) {
        final UnifiedPushMessage message = msg.getUnifiedPushMessage();
        final List<Variant> variants = msg.getVariants();
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
            } catch (BatchException e) {
                logger.severe("Failed to load batch of tokens", e);
            }
        }
    }
}