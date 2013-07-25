/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.jboss.aerogear.connectivity.service.sender.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.connectivity.api.Variant;
import org.jboss.aerogear.connectivity.message.sender.APNsPushNotificationSender;
import org.jboss.aerogear.connectivity.message.sender.GCMPushNotificationSender;
import org.jboss.aerogear.connectivity.message.sender.SimplePushNotificationSender;
import org.jboss.aerogear.connectivity.model.AndroidVariant;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.model.SimplePushVariant;
import org.jboss.aerogear.connectivity.model.iOSVariant;
import org.jboss.aerogear.connectivity.service.ClientInstallationService;
import org.jboss.aerogear.connectivity.service.GenericVariantService;
import org.jboss.aerogear.connectivity.service.sender.SenderService;
import org.jboss.aerogear.connectivity.service.sender.message.BroadcastMessage;
import org.jboss.aerogear.connectivity.service.sender.message.SelectiveSendCriterias;
import org.jboss.aerogear.connectivity.service.sender.message.SelectiveSendMessage;
import org.jboss.aerogear.connectivity.service.sender.message.UnifiedPushMessage;

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
    public void selectiveSend(PushApplication pushApplication, SelectiveSendMessage message) {
        logger.info(String.format("Processing 'selective send' request with '%s' payload", message));

        // collections for all the different variants:
        final Set<iOSVariant> iOSVariants = new HashSet<iOSVariant>();
        final Set<AndroidVariant> androidVariants = new HashSet<AndroidVariant>();
        final Set<SimplePushVariant> simplePushVariants = new HashSet<SimplePushVariant>();

        final SelectiveSendCriterias criterias = message.getSendCriterias();
        final List<String> variantIDs = criterias.getVariants();

        // if the "SelectiveSend" did specify the "variants" field,
        // we look up each of those mentioned variants, by their "variantID":
        if (variantIDs != null) {

            for (String variantID : variantIDs) {
                Variant variant = genericVariantService.findByVariantID(variantID);
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

        
        
        // TODO: DISPATCH TO A QUEUE .....
        for (iOSVariant iOSVariant : iOSVariants) {
            final List<String> selectiveTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(iOSVariant.getVariantID(), category , aliases, deviceTypes);
            this.sendToAPNs(iOSVariant, selectiveTokenPerVariant, message);
        }

        // TODO: DISPATCH TO A QUEUE .....
        for (AndroidVariant androidVariant : androidVariants) {
            final List<String> androidTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), category , aliases, deviceTypes);
            this.sendToGCM(androidTokenPerVariant, message, androidVariant.getGoogleKey());
        }


        // TODO: DISPATCH TO A QUEUE .....
        final Map<String, String> simplePushCategoriesAndValues = message.getSimplePush();
        // if no SimplePush object is present: skip it.
        // if there is a filter on "deviceTypes", but that contains NO 'web': skip it
        if (simplePushCategoriesAndValues == null || (deviceTypes != null && ! deviceTypes.contains("web"))) {
            return;
        }

        for (SimplePushVariant simplePushVariant : simplePushVariants) {
            // the specified category names.....
            final Set<String> simplePushCategories = simplePushCategoriesAndValues.keySet();
            // add empty list for every category:
            for (String simplePushCategory : simplePushCategories) {
                final List<String> tokensPerCategory = clientInstallationService.findAllSimplePushDeviceTokenForVariantIDByCriteria(simplePushVariant.getVariantID(), simplePushCategory, aliases);
                this.sentToSimplePush(simplePushVariant.getPushNetworkURL(), simplePushCategoriesAndValues.get(simplePushCategory), tokensPerCategory);
            }
        }
    }

    @Override
    @Asynchronous
    public void broadcast(PushApplication pushApplication, BroadcastMessage payload) {
        logger.info(String.format("Processing broadcast request with '%s' payload", payload));

        // TODO: DISPATCH TO A QUEUE .....
        final Set<iOSVariant> iOSVariants = pushApplication.getIOSVariants();
        for (iOSVariant iOSVariant : iOSVariants) {
            final List<String> iosTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantID(iOSVariant.getVariantID());
            this.sendToAPNs(iOSVariant, iosTokenPerVariant, payload);
        }

        // TODO: DISPATCH TO A QUEUE .....
        Set<AndroidVariant> androidVariants = pushApplication.getAndroidVariants();
        for (AndroidVariant androidVariant : androidVariants) {
            final List<String> androidTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantID(androidVariant.getVariantID());
            this.sendToGCM(androidTokenPerVariant, payload, androidVariant.getGoogleKey());
        }


        // TODO: DISPATCH TO A QUEUE .....
        final String simplePushBroadcastValue = payload.getSimplePush();
        if (simplePushBroadcastValue == null) {
            return;
        }

        Set<SimplePushVariant> simplePushVariants = pushApplication.getSimplePushVariants();
        for (SimplePushVariant simplePushVariant : simplePushVariants) {
            // by convention we use the "AeroGear-specific" broadcast category:
            // TODO: create SimplePush Service class
            final List<String> simplePushBroadcastTokens = clientInstallationService.findAllSimplePushBroadcastDeviceTokenForVariantID(simplePushVariant.getVariantID());
            this.sentToSimplePush(simplePushVariant.getPushNetworkURL(), simplePushBroadcastValue, simplePushBroadcastTokens);
        }
    }

    private void sendToAPNs(iOSVariant iOSVariant, Collection<String> tokens, UnifiedPushMessage pushMessage) {
        logger.fine(String.format("Sending: %s to APNs", pushMessage));
        apnsSender.sendPushMessage(iOSVariant, tokens, pushMessage);
    }

    private void sendToGCM(Collection<String> tokens, UnifiedPushMessage pushMessage, String apiKey) {
        logger.fine(String.format("Sending: %s to GCM", pushMessage));
        gcmSender.sendPushMessage(tokens, pushMessage, apiKey);
    }

    private void sentToSimplePush(String endpointBaseURL, String payload, List<String> channels) {
        logger.fine(String.format("Sending: %s to SimplePushServer ('%s')", payload, endpointBaseURL));
        simplePushSender.sendMessage(endpointBaseURL, payload, channels);
    }
}
