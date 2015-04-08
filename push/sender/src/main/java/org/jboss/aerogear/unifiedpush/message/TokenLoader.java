package org.jboss.aerogear.unifiedpush.message;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.dao.BatchException;
import org.jboss.aerogear.unifiedpush.dao.ResultsStream;
import org.jboss.aerogear.unifiedpush.message.jms.Dequeue;
import org.jboss.aerogear.unifiedpush.message.jms.DispatchToQueue;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

public class TokenLoader {

    private final static int BATCH_SIZE = 1000;

    private final AeroGearLogger logger = AeroGearLogger.getInstance(TokenLoader.class);

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    @DispatchToQueue
    private Event<MessageWithTokens> dispatchTokensEvent;

    public void loadAndQueueTokenBatch(@Observes @Dequeue MessageWithVariants msg) {
        final UnifiedPushMessage message = msg.getUnifiedPushMessage();
        final List<Variant> variants = msg.getVariants();
        logger.fine("Received message from queue: " + message.getMessage().getAlert());

        final Criteria criteria = message.getCriteria();
        final List<String> categories = criteria.getCategories();
        final List<String> aliases = criteria.getAliases();
        final List<String> deviceTypes = criteria.getDeviceTypes();

        for (Variant variant : variants) {
            ResultsStream<String> tokenStream = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(variant.getVariantID(), categories, aliases, deviceTypes)
                .fetchSize(BATCH_SIZE).executeQuery();

            try {
                ArrayList<String> tokens = new ArrayList<String>(BATCH_SIZE);
                for (int i = 0; i < BATCH_SIZE && tokenStream.next(); i++) {
                    tokens.add(tokenStream.get());
                }
                dispatchTokensEvent.fire(new MessageWithTokens(msg.getPushMessageInformation(), message, variant, tokens));
            } catch (BatchException e) {
                logger.severe("Failed to load batch of tokens", e);
            }
        }
    }
}