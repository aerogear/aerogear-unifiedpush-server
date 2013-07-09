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
package org.jboss.aerogear.connectivity.message.sender;

import java.util.Collection;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.aerogear.connectivity.message.cache.APNsCache;
import org.jboss.aerogear.connectivity.message.sender.annotations.APNsSender;
import org.jboss.aerogear.connectivity.model.iOSVariant;
import org.jboss.aerogear.connectivity.service.sender.message.UnifiedPushMessage;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

@APNsSender
public class APNsPushNotificationSender {

    @Inject
    private APNsCache apnsCache;

    @Inject
    private Logger logger;

    public void sendPushMessage(iOSVariant iOSVariant, Collection<String> tokens, UnifiedPushMessage pushMessage) {

        String apnsMessage = APNS.newPayload()
                    // adding recognized key values
                .alertBody(pushMessage.getAlert()) // alert dialog, in iOS
                .badge(pushMessage.getBadge()) // little badge icon update;
                .sound(pushMessage.getSound()) // sound to be played by app

                .customFields(pushMessage.getData()) // adding other (submitted) fields

                .build(); // build the JSON payload, for APNs 

        // look up the ApnsService from the cache:
        String staging = pushMessage.getStaging();
        ApnsService service = lookupApnsService(iOSVariant, staging);

        if (service != null) {
            service.push(tokens, apnsMessage);
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("No certificate for '");
            sb.append(staging);
            sb.append("' was found.\nCould not send messages to APNs");
            logger.severe(sb.toString());
        }
    }

    /**
     * If "staging" key is present, and it has the "development" value, the method returns
     * a cached (Sandbox) APNS-Service. If no staging key is provided: PRODUCTION APNS-Service is used
     * 
     * Null is returned if there is no "configuration" for the request stage 
     */
    private ApnsService lookupApnsService(iOSVariant iOSVariant, String stagingValue) {
        
        if ("development".equals(stagingValue)) {
            if (iOSVariant.getDevelopmentCertificate() != null && iOSVariant.getDevelopmentPassphrase() != null) {
                return apnsCache.getDevelopmentService(iOSVariant);
            }
        } else {
            if (iOSVariant.getProductionCertificate() != null && iOSVariant.getProductionPassphrase() != null) {
                return apnsCache.getProductionService(iOSVariant);
            }            
        }
        // null if the request "staging" could not be fulfilled
        return null;
    }
}
