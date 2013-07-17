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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.connectivity.message.sender.APNsPushNotificationSender;
import org.jboss.aerogear.connectivity.message.sender.GCMPushNotificationSender;
import org.jboss.aerogear.connectivity.message.sender.SimplePushNotificationSender;
import org.jboss.aerogear.connectivity.message.sender.annotations.APNsSender;
import org.jboss.aerogear.connectivity.message.sender.annotations.GCMSender;
import org.jboss.aerogear.connectivity.message.sender.annotations.SimplePushSender;
import org.jboss.aerogear.connectivity.model.AndroidVariant;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.model.SimplePushVariant;
import org.jboss.aerogear.connectivity.model.iOSVariant;
import org.jboss.aerogear.connectivity.service.ClientInstallationService;
import org.jboss.aerogear.connectivity.service.sender.SenderService;
import org.jboss.aerogear.connectivity.service.sender.message.BroadcastMessage;
import org.jboss.aerogear.connectivity.service.sender.message.SelectiveSendCriterias;
import org.jboss.aerogear.connectivity.service.sender.message.SelectiveSendMessage;

@Stateless
@Asynchronous
public class SenderServiceImpl implements SenderService {

    @Inject
    @GCMSender
    private GCMPushNotificationSender gcmSender;

    @Inject
    @APNsSender
    private APNsPushNotificationSender apnsSender;

    @Inject
    @SimplePushSender
    private SimplePushNotificationSender simplePushSender;
    
    @Inject
    private ClientInstallationService clientInstallationService;

    @Override
    @Asynchronous
    public void selectiveSend(PushApplication pushApplication, SelectiveSendMessage message) {

        // get all the criterias:
        final SelectiveSendCriterias criterias = message.getSendCriterias();
        final String category = criterias.getCategory();
        final List<String> aliases = criterias.getAliases();
        final List<String> deviceTypes = criterias.getDeviceTypes();

        // TODO: DISPATCH TO A QUEUE .....
        final Set<iOSVariant> iOSVariants = pushApplication.getIOSVariants();
        for (iOSVariant iOSVariant : iOSVariants) {
            final List<String> selectiveTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(iOSVariant.getVariantID(), category , aliases, deviceTypes);
            apnsSender.sendPushMessage(iOSVariant, selectiveTokenPerVariant, message);
        }

        // TODO: DISPATCH TO A QUEUE .....
        Set<AndroidVariant> androidVariants = pushApplication.getAndroidVariants();
        for (AndroidVariant androidVariant : androidVariants) {
            final List<String> androidTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantIDByCriteria(androidVariant.getVariantID(), category , aliases, deviceTypes);
            gcmSender.sendPushMessage(androidTokenPerVariant, message, androidVariant.getGoogleKey());
        }


        // TODO: DISPATCH TO A QUEUE .....
        final Map<String, String> simplePushCategoriesAndValues = message.getSimplePush();
        // if no SimplePush object is present: skip it.
        // if there is a filter on "deviceTypes", but that contains NO 'web': skip it
        if (simplePushCategoriesAndValues == null || (deviceTypes != null && ! deviceTypes.contains("web"))) {
            return;
        }

        Set<SimplePushVariant> spApps = pushApplication.getSimplePushVariants();
        for (SimplePushVariant simplePushVariant : spApps) {
            // the specified category names.....
            final Set<String> simplePushCategories = simplePushCategoriesAndValues.keySet();
            // add empty list for every category:
            for (String simplePushCategory : simplePushCategories) {
                final List<String> tokensPerCategory = clientInstallationService.findAllSimplePushDeviceTokenForVariantIDByCriteria(simplePushVariant.getVariantID(), simplePushCategory, aliases);
                simplePushSender.sendMessage(simplePushVariant.getPushNetworkURL(), simplePushCategoriesAndValues.get(simplePushCategory), tokensPerCategory);
            }
        }
    }

    @Override
    @Asynchronous
    public void broadcast(PushApplication pushApplication, BroadcastMessage payload) {

        // TODO: DISPATCH TO A QUEUE .....
        final Set<iOSVariant> iOSVariants = pushApplication.getIOSVariants();
        for (iOSVariant iOSVariant : iOSVariants) {
            final List<String> iosTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantID(iOSVariant.getVariantID());
            apnsSender.sendPushMessage(iOSVariant, iosTokenPerVariant, payload);
        }

        // TODO: DISPATCH TO A QUEUE .....
        Set<AndroidVariant> androidVariants = pushApplication.getAndroidVariants();
        for (AndroidVariant androidVariant : androidVariants) {
            final List<String> androidTokenPerVariant = clientInstallationService.findAllDeviceTokenForVariantID(androidVariant.getVariantID());
            gcmSender.sendPushMessage(androidTokenPerVariant, payload, androidVariant.getGoogleKey());
        }


        // TODO: DISPATCH TO A QUEUE .....
        final String simplePushBroadcastValue = payload.getSimplePush();
        if (simplePushBroadcastValue == null) {
            return;
        }

        Set<SimplePushVariant> simplePushVariants = pushApplication.getSimplePushVariants();
        for (SimplePushVariant simplePushVariant : simplePushVariants) {
            // by convention we use the "AeroGear-specific" broadcast category:
            // TODO: create SimplePusj Service class
            final List<String> simplePushBroadcastTokens = clientInstallationService.findAllSimplePushBroadcastDeviceTokenForVariantID(simplePushVariant.getVariantID());
            simplePushSender.sendMessage(simplePushVariant.getPushNetworkURL(), simplePushBroadcastValue, simplePushBroadcastTokens);
        }
    }
}
