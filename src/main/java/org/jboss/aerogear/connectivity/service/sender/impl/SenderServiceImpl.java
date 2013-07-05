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
import org.jboss.aerogear.connectivity.service.MobileVariantInstanceService;
import org.jboss.aerogear.connectivity.service.sender.SenderService;
import org.jboss.aerogear.connectivity.service.sender.message.BroadcastMessage;
import org.jboss.aerogear.connectivity.service.sender.message.SelectiveSendMessage;

@Stateless
@Asynchronous
public class SenderServiceImpl implements SenderService {

    private static final String BROADCAST_CHANNEL = "broadcast";

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
    private MobileVariantInstanceService mobileVariantInstanceService;

    @Override
    @Asynchronous
    public void sendToAliases(PushApplication pushApplication, SelectiveSendMessage message) {

        // get all the criterias:
        final List<String> submittedAliases = message.getAliases();
        final List<String> submittedDeviceTypes = message.getDeviceTypes();
        // TODO: Add getCategory();

        // TODO: DISPATCH TO A QUEUE .....
        final Set<iOSVariant> iOSVariants = pushApplication.getIOSApps();
        for (iOSVariant iOSVariant : iOSVariants) {
            final List<String> selectiveTokenPerVariant = mobileVariantInstanceService.findAllDeviceTokenForVariantIDByAliasAndDeviceType(iOSVariant.getVariantID(), submittedAliases, submittedDeviceTypes);
            apnsSender.sendPushMessage(iOSVariant, selectiveTokenPerVariant, message);
        }

        // TODO: DISPATCH TO A QUEUE .....
        Set<AndroidVariant> androidVariants = pushApplication.getAndroidApps();
        for (AndroidVariant androidVariant : androidVariants) {
            final List<String> androidTokenPerVariant = mobileVariantInstanceService.findAllDeviceTokenForVariantIDByAliasAndDeviceType(androidVariant.getVariantID(), submittedAliases, submittedDeviceTypes);
            gcmSender.sendPushMessage(androidTokenPerVariant, message, androidVariant.getGoogleKey());
        }

        // TODO: DISPATCH TO A QUEUE .....
        final Map<String, String> simplePushCategoriesAndValues = message.getSimplePush();

        // if no SimplePush object is present: skip it.
        // if there is a filter on "deviceTypes", but that contains NO 'web': skip it
        if (simplePushCategoriesAndValues == null || (submittedDeviceTypes != null && ! submittedDeviceTypes.contains("web"))) {
            return;
        }

        Set<SimplePushVariant> spApps = pushApplication.getSimplePushApps();
        for (SimplePushVariant simplePushVariant : spApps) {
            // the specified category names.....
            final Set<String> categoriesToNotify = simplePushCategoriesAndValues.keySet();
            // add empty list for every category:
            for (String category : categoriesToNotify) {
                final List<String> tokensPerCategory = mobileVariantInstanceService.findAllDeviceTokenForVariantIDByCategoryAndAlias(simplePushVariant.getVariantID(), category, submittedAliases);
                simplePushSender.sendMessage(simplePushVariant.getPushNetworkURL(), simplePushCategoriesAndValues.get(category), tokensPerCategory);
            }
        }
    }

    @Override
    @Asynchronous
    public void broadcast(PushApplication pushApplication, BroadcastMessage payload) {

        // TODO: DISPATCH TO A QUEUE .....
        final Set<iOSVariant> iOSVariants = pushApplication.getIOSApps();
        for (iOSVariant iOSVariant : iOSVariants) {
            final List<String> iosTokenPerVariant = mobileVariantInstanceService.findAllDeviceTokenForVariantID(iOSVariant.getVariantID());
            apnsSender.sendPushMessage(iOSVariant, iosTokenPerVariant, payload);
        }

        // TODO: DISPATCH TO A QUEUE .....
        Set<AndroidVariant> androidVariants = pushApplication.getAndroidApps();
        for (AndroidVariant androidVariant : androidVariants) {
            final List<String> androidTokenPerVariant = mobileVariantInstanceService.findAllDeviceTokenForVariantID(androidVariant.getVariantID());
            gcmSender.sendPushMessage(androidTokenPerVariant, payload, androidVariant.getGoogleKey());
        }

        // TODO: DISPATCH TO A QUEUE .....
        final String simplePushBroadcastValue = payload.getSimplePush();
        if (simplePushBroadcastValue == null) {
            return;
        }

        Set<SimplePushVariant> simplePushVariants = pushApplication.getSimplePushApps();
        for (SimplePushVariant simplePushVariant : simplePushVariants) {
            // by convention we use the "AeroGear-specific" broadcast category:
            // TODO: create SimplePusj Service class
            final List<String> simplePushBroadcastTokens = mobileVariantInstanceService.findAllDeviceTokenForVariantIDByCategory(simplePushVariant.getVariantID(), BROADCAST_CHANNEL);
            simplePushSender.sendMessage(simplePushVariant.getPushNetworkURL(), simplePushBroadcastValue, simplePushBroadcastTokens);
        }
    }
}
