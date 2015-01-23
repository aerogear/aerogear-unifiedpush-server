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
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderTypeLiteral;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
@Asynchronous
public class SenderServiceImpl implements SenderService {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(SenderServiceImpl.class);

    @Inject
    @Any
    private Instance<PushNotificationSender> senders;

    @Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private GenericVariantService genericVariantService;
    @Inject
    private PushMessageMetricsService metricsService;

    @Override
    @Asynchronous
    public void send(PushApplication pushApplication, UnifiedPushMessage message) {
        logger.info("Processing send request with '" + message.toString() + "' payload");

        final PushMessageInformation pushMessageInformation =
                metricsService.storeNewRequestFrom(
                        pushApplication.getPushApplicationID(),
                        message.toJsonString(),
                        message.getIpAddress(),
                        message.getClientIdentifier()
                        );

        // collections for all the different variants:
        final Set<Variant> variants = new HashSet<Variant>();

        final Criteria criteria = message.getCriteria();
        final List<String> variantIDs = criteria.getVariants();

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

        // all possible criteria
        final List<String> categories = criteria.getCategories();
        final List<String> aliases = criteria.getAliases();
        final List<String> deviceTypes = criteria.getDeviceTypes();

        // TODO: DISPATCH TO A QUEUE .....
        for (final Variant variant : variants) {
            final List<String> tokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(variant.getVariantID(), categories, aliases, deviceTypes);
            senders.select(new SenderTypeLiteral(variant.getClass())).get()
                    .sendPushMessage(variant, tokenPerVariant, message, new SenderServiceCallback(variant, tokenPerVariant.size(), pushMessageInformation));
        }
    }

    /**
     * Helpers to update the given {@link PushMessageInformation} with a {@link VariantMetricInformation} object
     */
    private void updateStatusOfPushMessageInformation(final PushMessageInformation pushMessageInformation, final String variantID, final int receives, final Boolean deliveryStatus) {
        this.updateStatusOfPushMessageInformation(pushMessageInformation, variantID, receives, deliveryStatus, null);
    }

    private void updateStatusOfPushMessageInformation(final PushMessageInformation pushMessageInformation, final String variantID, final int receives, final Boolean deliveryStatus, final String reason) {
        final VariantMetricInformation variantMetricInformation = new VariantMetricInformation();
        variantMetricInformation.setVariantID(variantID);
        variantMetricInformation.setReceivers(receives);
        variantMetricInformation.setDeliveryStatus(deliveryStatus);
        variantMetricInformation.setReason(reason);
        pushMessageInformation.addVariantInformations(variantMetricInformation);
        pushMessageInformation.setTotalReceivers(pushMessageInformation.getTotalReceivers() + receives);

        // store it!
        metricsService.updatePushMessageInformation(pushMessageInformation);
    }

    private class SenderServiceCallback implements NotificationSenderCallback {
        private final Variant variant;
        private final int tokenSize;
        private final PushMessageInformation pushMessageInformation;

        public SenderServiceCallback(Variant variant, int tokenSize, PushMessageInformation pushMessageInformation) {
            this.variant = variant;
            this.tokenSize = tokenSize;
            this.pushMessageInformation = pushMessageInformation;
        }

        @Override
        public void onSuccess() {
            logger.fine(String.format("Sent '%s' message to '%d' devices", variant.getType().getTypeName(), tokenSize));
            updateStatusOfPushMessageInformation(pushMessageInformation, variant.getVariantID(), tokenSize, Boolean.TRUE);
        }

        @Override
        public void onError(final String reason) {
            logger.warning(String.format("Error on '%s' delivery", variant.getType().getTypeName()));
            updateStatusOfPushMessageInformation(pushMessageInformation, variant.getVariantID(), tokenSize, Boolean.FALSE, reason);
        }
    }
}
