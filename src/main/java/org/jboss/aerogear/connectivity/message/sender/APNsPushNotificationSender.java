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

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.aerogear.connectivity.message.cache.APNsCache;
import org.jboss.aerogear.connectivity.model.iOSVariant;
import org.jboss.aerogear.connectivity.service.sender.message.UnifiedPushMessage;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

public class APNsPushNotificationSender {

    @Inject
    APNsCache apnsCache;

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
        ApnsService service = lookupApnsService(iOSVariant);

        if (service != null) {
            try {
                // send:
                service.start();
                service.push(tokens, apnsMessage);
            }
            finally {
                // tear down and release resources:
                service.stop();
            }
        } else {
            logger.severe("No certificate was found. Could not send messages to APNs");
        }
    }

    /**
     * Returns the ApnsService, based on the required profile (production VS sandbox/test).
     * Null is returned if there is no "configuration" for the request stage 
     */
    private ApnsService lookupApnsService(iOSVariant iOSVariant) {

        // this check should not be needed, but you never know:
        if (iOSVariant.getCertificate() != null && iOSVariant.getPassphrase() != null) {

            if (iOSVariant.isProduction()) {

                return APNS
                        .newService()
                        .withCert(
                                new ByteArrayInputStream(iOSVariant.getCertificate()),
                                iOSVariant.getPassphrase()).withProductionDestination()
                               .asQueued().build();
            } else {

                return APNS
                        .newService()
                        .withCert(
                                new ByteArrayInputStream(iOSVariant.getCertificate()),
                                iOSVariant.getPassphrase()).withSandboxDestination()
                               .asQueued().build();
            }
        }
        // null if the request "staging" could not be fulfilled
        return null;
    }
}
