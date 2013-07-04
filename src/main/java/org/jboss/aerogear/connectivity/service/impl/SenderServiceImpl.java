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
package org.jboss.aerogear.connectivity.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.aerogear.connectivity.message.sender.APNsPushNotificationSender;
import org.jboss.aerogear.connectivity.message.sender.GCMPushNotificationSender;
import org.jboss.aerogear.connectivity.message.sender.SimplePushNotificationSender;
import org.jboss.aerogear.connectivity.message.sender.UnifiedPushMessage;
import org.jboss.aerogear.connectivity.message.sender.annotations.APNsSender;
import org.jboss.aerogear.connectivity.message.sender.annotations.GCMSender;
import org.jboss.aerogear.connectivity.message.sender.annotations.SimplePushSender;
import org.jboss.aerogear.connectivity.model.AndroidVariant;
import org.jboss.aerogear.connectivity.model.MobileVariantInstanceImpl;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.model.SimplePushVariant;
import org.jboss.aerogear.connectivity.model.iOSVariant;
import org.jboss.aerogear.connectivity.rest.sender.messages.SelectiveSendMessage;
import org.jboss.aerogear.connectivity.service.SenderService;

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

    @Override
    @Asynchronous
    public void sendToAliases(PushApplication pushApplication, SelectiveSendMessage message) {

        final List<String> submittedAliases = message.getAliases();
        final UnifiedPushMessage unifiedPushMessage = new UnifiedPushMessage(message.getMessage());

        // TODO: Make better...
        final Set<iOSVariant> iOSapps = pushApplication.getIOSApps();
        for (iOSVariant iOSApp : iOSapps) {

            final List<String> iOSTokenPerVariant = new ArrayList<String>();
            // get all instances
            final Set<MobileVariantInstanceImpl> instancesPerVariant = iOSApp.getInstances();
            for (MobileVariantInstanceImpl instance : instancesPerVariant) {

                // see if the alias does match for the instance
                if (submittedAliases != null && submittedAliases.contains(instance.getAlias())) {
                    // add it
                    iOSTokenPerVariant.add(instance.getDeviceToken());
                }
            }
            // deliver to network
            apnsSender.sendPushMessage(iOSApp, iOSTokenPerVariant, unifiedPushMessage);
        }

        // TODO: make better :)
        Set<AndroidVariant> androidApps = pushApplication.getAndroidApps();
        for (AndroidVariant androidApplication : androidApps) {

            final List<String> androidTokenPerVariant = new ArrayList<String>();
            //final GCMPushNotificationSender gcmSender = new GCMPushNotificationSender(androidApplication.getGoogleKey());

            // get all instances
            Set<MobileVariantInstanceImpl> instancesPerVariant = androidApplication.getInstances();
            for (MobileVariantInstanceImpl instance : instancesPerVariant) {

                // see if the alias does match for the instance
                if (submittedAliases != null && submittedAliases.contains(instance.getAlias())) {
                    // add it
                    androidTokenPerVariant.add(instance.getDeviceToken());
                }
            }
            gcmSender.sendPushMessage(androidTokenPerVariant, unifiedPushMessage, androidApplication.getGoogleKey());
        }

        // TODO: make better :)
        final Map<String, String> simplePushCategoriesAndValues = message.getSimplePush();
        if (simplePushCategoriesAndValues == null) {
            return;
        }

        Set<SimplePushVariant> spApps = pushApplication.getSimplePushApps();
        for (SimplePushVariant simplePushVariant : spApps) {

            // the specified category names.....
            final Set<String> categoriesToNotify = simplePushCategoriesAndValues.keySet();
            final Map<String, List<String>> tokensPerCategory = new LinkedHashMap<String, List<String>>();

            // add empty list for every category:
            for (String category : categoriesToNotify) {
                tokensPerCategory.put(category, new ArrayList<String>());
            }

            Set<MobileVariantInstanceImpl> allSimplePushVarinatInstancesPerVariant = simplePushVariant.getInstances();
            for (MobileVariantInstanceImpl instance : allSimplePushVarinatInstancesPerVariant) {

                String categoryFromInstance = instance.getCategory();
                // Does the category match one of the submitted ones?
                // Does the alias also match ??
                if (tokensPerCategory.get(categoryFromInstance) != null) {

                    String currentAlias = instance.getAlias();
                    // NO alias at all .....???
                    // alias matches......
                    if ((submittedAliases == null && currentAlias == null) || (submittedAliases.contains(currentAlias)))

                        // add the token, to the matching category list:
                        tokensPerCategory.get(categoryFromInstance).add(instance.getDeviceToken());
                }
            }
            // send:
            for (String category : categoriesToNotify) {
                simplePushSender.sendMessage(simplePushVariant.getPushNetworkURL(), simplePushCategoriesAndValues.get(category), tokensPerCategory.get(category));
            }
        }
    }

    @Override
    @Asynchronous
    public void broadcast(PushApplication pushApplication,
            Map<String, ? extends Object> jsonMap) {

        final UnifiedPushMessage unifiedPushMessage = new UnifiedPushMessage(jsonMap);

        // TODO: DISPATCH TO A QUEUE .....
        final Set<iOSVariant> iOSapps = pushApplication.getIOSApps();
        for (iOSVariant iOSApp : iOSapps) {

            // get all the tokens:
            final Set<String> iOStokenz = new HashSet<String>();
            Set<MobileVariantInstanceImpl> iOSinstallations = iOSApp
                    .getInstances();
            for (MobileVariantInstanceImpl mobileApplicationInstance : iOSinstallations) {
                iOStokenz.add(mobileApplicationInstance.getDeviceToken());
            }

            apnsSender.sendPushMessage(iOSApp, iOStokenz, unifiedPushMessage);
        }

        // TODO: DISPATCH TO A QUEUE .....
        Set<AndroidVariant> androidApps = pushApplication.getAndroidApps();
        for (AndroidVariant androidApplication : androidApps) {

            final List<String> androidtokenz = new ArrayList<String>();

            Set<MobileVariantInstanceImpl> androidApplications = androidApplication
                    .getInstances();
            for (MobileVariantInstanceImpl mobileApplicationInstance : androidApplications) {
                androidtokenz.add(mobileApplicationInstance.getDeviceToken());
            }
            gcmSender.sendPushMessage(androidtokenz, unifiedPushMessage, androidApplication.getGoogleKey());
        }

        // TODO: DISPATCH TO A QUEUE .....
        Set<SimplePushVariant> spApps = pushApplication.getSimplePushApps();
        for (SimplePushVariant simplePushVariant : spApps) {

            final List<String> simplePushTokenz = new ArrayList<String>();

            Set<MobileVariantInstanceImpl> simplePushVarinatInstances = simplePushVariant.getInstances();
            for (MobileVariantInstanceImpl instance : simplePushVarinatInstances) {
                // only the BROADCAST channels:
                if ("broadcast".equalsIgnoreCase(instance.getCategory())) {
                    simplePushTokenz.add(instance.getDeviceToken());
                }
            }
            simplePushSender.sendMessage(
                    simplePushVariant.getPushNetworkURL(),
                    (String) unifiedPushMessage.getData().get("simple-push"), // TODO: add a getter for simple-push
                    simplePushTokenz);
        }
    }
}
