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
package org.jboss.aerogear.unifiedpush.service.sender.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.sender.APNsPushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.GCMPushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SimplePushNotificationSender;
import org.jboss.aerogear.unifiedpush.model.AndroidVariant;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.sender.SenderService;
import org.jboss.aerogear.unifiedpush.service.sender.message.SendCriterias;
import org.jboss.aerogear.unifiedpush.service.sender.message.UnifiedPushMessageImpl;
import org.jboss.aerogear.unifiedpush.service.sender.message.UnifiedPushMessage;

@Stateless
@Asynchronous
public class SenderServiceImpl implements SenderService {

    @Inject
    private GCMPushNotificationSender gcmSender;

    @Inject
    private APNsPushNotificationSender apnsSender;

    @Inject
    private SimplePushNotificationSender simplePushSender;

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    private GenericVariantService genericVariantService;

    @Inject
    private Logger logger;

    @Override
    @Asynchronous
    public void send(PushApplication pushApplication, UnifiedPushMessageImpl message) {
        logger.info(String.format("Processing send request with '%s' payload", message));

        // collections for all the different variants:
        final Set<iOSVariant> iOSVariants = new HashSet<iOSVariant>();
        final Set<AndroidVariant> androidVariants = new HashSet<AndroidVariant>();
        final Set<SimplePushVariant> simplePushVariants = new HashSet<SimplePushVariant>();

        final SendCriterias criterias = message.getSendCriterias();
        final List<String> variantIDs = criterias.getVariants();

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
                    default:
                        // nope; should never enter here
                        break;
                    }
                }
            }
        } else {
            // No specific variants have been requested,
            // we get all the variants, from the given PushApplication:
            androidVariants.addAll(pushApplication.getAndroidVariants());
            iOSVariants.addAll(pushApplication.getIOSVariants());
            simplePushVariants.addAll(pushApplication.getSimplePushVariants());
        }

        // all possible criteria
        final String category = criterias.getCategory();
        final List<String> aliases = criterias.getAliases();
        final List<String> deviceTypes = criterias.getDeviceTypes();

        // let's check if we actually have data for native platforms!
        if (message.getData() != null) {

            // TODO: DISPATCH TO A QUEUE .....
            for (iOSVariant iOSVariant : iOSVariants) {
                final List<String> tokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(iOSVariant.getVariantID(), category,
                        aliases, deviceTypes);
                this.sendToAPNs(iOSVariant, tokenPerVariant, message);
            }

            // TODO: DISPATCH TO A QUEUE .....
            for (AndroidVariant androidVariant : androidVariants) {
                final List<String> androidTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), category,
                        aliases, deviceTypes);
                this.sendToGCM(androidVariant, androidTokenPerVariant, message);
            }
        }

        // TODO: DISPATCH TO A QUEUE .....
        final Map<String, String> simplePushCategoriesAndValues = message.getSimplePush();
        // if no SimplePush object is present: skip it.
        // if there is a filter on "deviceTypes", but that contains NO 'web': skip it
        if (simplePushCategoriesAndValues == null || (deviceTypes != null && !deviceTypes.contains("web"))) {
            return;
        }

        for (SimplePushVariant simplePushVariant : simplePushVariants) {
            // the specified category names.....
            final Set<String> simplePushCategories = simplePushCategoriesAndValues.keySet();
            // add empty list for every category:
            for (String simplePushCategory : simplePushCategories) {

                final List<String> pushEndpointURLsPerCategory = clientInstallationService.findAllSimplePushEndpointURLsForVariantIDByCriteria(simplePushVariant
                        .getVariantID(), simplePushCategory, aliases);
                this.sentToSimplePush(pushEndpointURLsPerCategory, simplePushCategoriesAndValues.get(simplePushCategory));
            }
        }
    }

    private void sendToAPNs(iOSVariant iOSVariant, Collection<String> tokens, UnifiedPushMessage pushMessage) {
        logger.fine(String.format("Sending: %s to APNs", pushMessage));
        apnsSender.sendPushMessage(iOSVariant, tokens, pushMessage);
    }

    private void sendToGCM(AndroidVariant androidVariant, List<String> tokens, UnifiedPushMessage pushMessage) {
        logger.fine(String.format("Sending: %s to GCM", pushMessage));
        gcmSender.sendPushMessage(androidVariant, tokens, pushMessage);
    }

    private void sentToSimplePush(List<String> pushEndpointURLs, String payload) {
        logger.fine(String.format("Sending: %s to SimplePush network/server", payload));
        simplePushSender.sendMessage(pushEndpointURLs, payload);
    }
}
