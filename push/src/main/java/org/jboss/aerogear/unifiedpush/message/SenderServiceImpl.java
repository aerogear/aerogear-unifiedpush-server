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

import org.jboss.aerogear.unifiedpush.api.*;
import org.jboss.aerogear.unifiedpush.message.sender.*;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Asynchronous
public class SenderServiceImpl implements SenderService {

    private final Logger logger = Logger.getLogger(SenderServiceImpl.class.getName());
    private final SimplePushNotificationSender simplePushSender = new SimplePushNotificationSender();
    private final GCMForChromePushNotificationSender gcmForChromePushNotificationSender = new GCMForChromePushNotificationSender();

    @Inject
    private GCMPushNotificationSender gcmSender;
    @Inject
    private APNsPushNotificationSender apnsSender;
    @Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private GenericVariantService genericVariantService;
    @Inject
    private PushMessageMetricsService metricsService;

    @Override
    @Asynchronous
    public void send(PushApplication pushApplication, UnifiedPushMessage message) {
        logger.log(Level.INFO, "Processing send request with '" + message.toString() + "' payload");

        final PushMessageInformation pushMessageInformation =
                metricsService.storeNewRequestFrom(
                        pushApplication.getPushApplicationID(),
                        message.toJsonString(),
                        message.getIpAddress(),
                        message.getClientIdentifier()
                );

        // collections for all the different variants:
        final Set<iOSVariant> iOSVariants = new HashSet<iOSVariant>();
        final Set<AndroidVariant> androidVariants = new HashSet<AndroidVariant>();
        final Set<SimplePushVariant> simplePushVariants = new HashSet<SimplePushVariant>();
        final Set<ChromePackagedAppVariant> chromePackagedAppVariants = new HashSet<ChromePackagedAppVariant>();

        final SendCriteria criteria = message.getSendCriteria();
        final List<String> variantIDs = criteria.getVariants();

        // if the criteria payload did specify the "variants" field,
        // we look up each of those mentioned variants, by their "variantID":
        if (variantIDs != null) {

            for (String variantID : variantIDs) {
                Variant variant = genericVariantService.findByVariantID(variantID);

                // does the variant exist ? 
                if (variant != null) {

                    // based on type, we store in the matching collection
                    switch (variant.getType()) {
                    case ANDROID:
                        androidVariants.add((AndroidVariant) variant);
                        break;
                    case IOS:
                        iOSVariants.add((iOSVariant) variant);
                        break;
                    case SIMPLE_PUSH:
                        simplePushVariants.add((SimplePushVariant) variant);
                        break;
                    case CHROME_PACKAGED_APP:
                        chromePackagedAppVariants.add((ChromePackagedAppVariant) variant);
                    default:
                        // nope; should never enter here
                        break;
                    }
                }
            }
        } else {
            // No specific variants have been requested,
            // we get all the variants, from the given PushApplicationEntity:
            androidVariants.addAll(pushApplication.getAndroidVariants());
            iOSVariants.addAll(pushApplication.getIOSVariants());
            simplePushVariants.addAll(pushApplication.getSimplePushVariants());
            chromePackagedAppVariants.addAll(pushApplication.getChromePackagedAppVariants());
        }

        // all possible criteria
        final List<String> categories = criteria.getCategories();
        final List<String> aliases = criteria.getAliases();
        final List<String> deviceTypes = criteria.getDeviceTypes();

        // let's check if we actually have data for native platforms!
        if (message.getData() != null) {

            // TODO: DISPATCH TO A QUEUE .....
            for (final iOSVariant iOSVariant : iOSVariants) {
                final List<String> tokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(iOSVariant.getVariantID(), categories, aliases, deviceTypes);
                apnsSender.sendPushMessage(iOSVariant, tokenPerVariant, message, new NotificationSenderCallback() {
                    @Override
                    public void onSuccess() {
                        logger.log(Level.FINE, "Sent APNs message to '" + tokenPerVariant.size() + "' devices");
                        updateStatusOfPushMessageInformation(pushMessageInformation, iOSVariant.getVariantID(), tokenPerVariant.size(), Boolean.TRUE) ;
                    }

                    @Override
                    public void onError() {
                        logger.log(Level.WARNING, "Error on APNs delivery");
                        updateStatusOfPushMessageInformation(pushMessageInformation, iOSVariant.getVariantID(), tokenPerVariant.size(), Boolean.FALSE) ;
                    }
                });

            }

            // TODO: DISPATCH TO A QUEUE .....
            for (final AndroidVariant androidVariant : androidVariants) {
                final List<String> androidTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), categories, aliases, deviceTypes);
                gcmSender.sendPushMessage(androidVariant, androidTokenPerVariant, message, new NotificationSenderCallback() {
                    @Override
                    public void onSuccess() {
                        logger.log(Level.FINE, "Sent GCM-Android message to '" + androidTokenPerVariant.size() + "' devices");
                        updateStatusOfPushMessageInformation(pushMessageInformation, androidVariant.getVariantID(), androidTokenPerVariant.size(), Boolean.TRUE) ;
                    }

                    @Override
                    public void onError() {
                        logger.log(Level.WARNING, "Error on GCM-Android delivery");
                        updateStatusOfPushMessageInformation(pushMessageInformation, androidVariant.getVariantID(), androidTokenPerVariant.size(), Boolean.FALSE) ;
                    }
                });
            }

            // TODO: DISPATCH TO A QUEUE .....
            for(final ChromePackagedAppVariant chromePackagedAppVariant : chromePackagedAppVariants) {
                final List<String> chromePackagedAppTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(chromePackagedAppVariant.getVariantID(), categories, aliases, deviceTypes);
                logger.log(Level.FINE, "Sending Chrome/GCM message to '" + chromePackagedAppTokenPerVariant.size() + "' devices");
                gcmForChromePushNotificationSender.sendPushMessage(chromePackagedAppVariant, chromePackagedAppTokenPerVariant, message, new NotificationSenderCallback() {
                    @Override
                    public void onSuccess() {
                        logger.log(Level.FINE, "Sent GCM-Chrome message to '" + chromePackagedAppTokenPerVariant.size() + "' devices");
                        updateStatusOfPushMessageInformation(pushMessageInformation, chromePackagedAppVariant.getVariantID(), chromePackagedAppTokenPerVariant.size(), Boolean.TRUE) ;
                    }

                    @Override
                    public void onError() {
                        logger.log(Level.WARNING, "Error on GCM-Chrome  delivery");
                        updateStatusOfPushMessageInformation(pushMessageInformation, chromePackagedAppVariant.getVariantID(), chromePackagedAppTokenPerVariant.size(), Boolean.FALSE) ;
                    }
                });
            }
        }


        for (final SimplePushVariant simplePushVariant : simplePushVariants) {
            final List<String> pushEndpointURLsPerCategory = clientInstallationService.findAllSimplePushEndpointURLsForVariantIDByCriteria(simplePushVariant .getVariantID(), categories, aliases, deviceTypes);
            simplePushSender.sendPushMessage(simplePushVariant, pushEndpointURLsPerCategory, message, new NotificationSenderCallback() {
                @Override
                public void onSuccess() {
                    logger.log(Level.FINE, "Sent SimplePush message to '" + pushEndpointURLsPerCategory.size() + "' devices");
                    updateStatusOfPushMessageInformation(pushMessageInformation, simplePushVariant.getVariantID(), pushEndpointURLsPerCategory.size(), Boolean.TRUE) ;
                }

                @Override
                public void onError() {
                    logger.log(Level.WARNING, "Error on SimplePush delivery");
                    updateStatusOfPushMessageInformation(pushMessageInformation, simplePushVariant.getVariantID(), pushEndpointURLsPerCategory.size(), Boolean.FALSE) ;
                }
            });
        }
    }

    /**
     * Helper to update the given {@link PushMessageInformation} with a {@link VariantMetricInformation} object
     */
    private void updateStatusOfPushMessageInformation(PushMessageInformation pushMessageInformation, String variantID, int receives, Boolean deliveryStatus) {
        final VariantMetricInformation variantMetricInformation = new VariantMetricInformation();
        variantMetricInformation.setVariantID(variantID);
        variantMetricInformation.setReceivers(receives);
        variantMetricInformation.setDeliveryStatus(deliveryStatus);
        pushMessageInformation.getVariantInformations().add(variantMetricInformation);

        // store it!
        metricsService.updatePushMessageInformation(pushMessageInformation);
    }
}
