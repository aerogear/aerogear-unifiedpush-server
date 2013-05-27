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

package org.aerogear.connectivity.message.sender;

import java.io.ByteArrayInputStream;
import java.util.Collection;

import org.aerogear.connectivity.message.sender.annotations.APNsSender;

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

@APNsSender
public class APNsPushNotificationSender implements PushNotificationSender {
    
    private final ApnsService service;
    
    public APNsPushNotificationSender(byte[] certificate, String passphrase) {
        // service PER iOS variant
        service = APNS
                .newService()
                .withCert(
                        new ByteArrayInputStream(certificate),
                        passphrase).withSandboxDestination()
                .asQueued().build();
    }
    
    @Override
    public void sendPushMessage(Collection<String> tokens, UnifiedPushMessage pushMessage) {
        try {
            String apnsMessage = APNS.newPayload()
                    // adding recognized key values
                    .alertBody(pushMessage.getAlert())    // alert dialog, in iOS
                    .badge(pushMessage.getBadge())        // little badge icon update;
                    .sound(pushMessage.getSound())        // sound to be played by app

                    .customFields(pushMessage.getData())  // adding other (submitted) fields

                    .build();  // build the JSON payload, for APNs 

            service.push(tokens, apnsMessage);
        } finally {
            // clean up the resources!
            service.stop();
        }
    }
}
