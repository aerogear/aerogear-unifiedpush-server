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

package org.aerogear.connectivity.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aerogear.connectivity.model.AndroidApplication;
import org.aerogear.connectivity.model.MobileApplicationInstance;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.model.iOSApplication;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.Sender;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

public class SenderServiceImpl implements SenderService {

    @Override
    public void broadcast(PushApplication pushApp,
            Map<String, String> jsonMap) {
        
        
        final UnifiedPushMessage message = new UnifiedPushMessage(jsonMap);
        
        // TODO: DISPATCH TO A QUEUE .....
        Set<iOSApplication> iOSapps = pushApp.getIOSApps();
        for (iOSApplication iOSApp : iOSapps) {

            // service PER iOS app
            ApnsService service = APNS
                    .newService()
                    .withCert(
                            new ByteArrayInputStream(iOSApp.getCertificate()),
                            iOSApp.getPassphrase()).withSandboxDestination()
                    .asQueued().build();

            // get all the tokens:
            final Set<String> iOStokenz = new HashSet<String>();
            Set<MobileApplicationInstance> iOSinstallations = iOSApp
                    .getInstances();
            for (MobileApplicationInstance mobileApplicationInstance : iOSinstallations) {
                iOStokenz.add(mobileApplicationInstance.getDeviceToken());
            }

            String apnsMessage = APNS.newPayload()
                    // adding recognized key values
                    .alertBody(message.alert)    // alert dialog, in iOS
                    .badge(message.badge)        // little badge icon update;
                    .sound(message.sound)        // sound to be played by app
                    
                    .customFields(message.data)  // adding other (submitted) fields
                    .build();                    // build the JSON payload, for APNs 
            // send it out:
            service.push(iOStokenz, apnsMessage);
        }

        // TODO: DISPATCH TO A QUEUE .....
        Set<AndroidApplication> androidApps = pushApp.getAndroidApps();
        for (AndroidApplication androidApplication : androidApps) {

            // service PER android app:
            Sender sender = new Sender(androidApplication.getGoogleKey());

            final List<String> androidtokenz = new ArrayList<String>();
            Set<MobileApplicationInstance> androidApplications = androidApplication
                    .getInstances();
            for (MobileApplicationInstance mobileApplicationInstance : androidApplications) {
                androidtokenz.add(mobileApplicationInstance.getDeviceToken());
            }

            // payload builder:
            Builder gcmBuilder = new Message.Builder();
            
            // add the "regconized" keys...
            gcmBuilder.addData("alert", message.alert);
            gcmBuilder.addData("sound", message.sound);
            gcmBuilder.addData("badge", ""+message.badge);
            
            // iterate over the missing keys:
            Set<String> keys = message.data.keySet();
            for (String key : keys) {
                gcmBuilder.addData(key, message.data.get(key));
            }
            
            Message gcmMessage = gcmBuilder.build();

            // send it out.....
            try {
                sender.send(gcmMessage, androidtokenz, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    
    // internal helper class
    class UnifiedPushMessage {
        String alert;
        String sound;
        int badge;
        Map<String, String> data;
        public UnifiedPushMessage(Map<String, String> data) {
            // special key words (for APNs)
            this.alert = data.remove("alert");
            this.sound = data.remove("sound");
            
            String badgeVal = data.remove("badge");
            if (badgeVal == null) {
                this.badge = -1;
            } else {
                this.badge = Integer.parseInt(badgeVal);
            }

            // rest of the data:
            this.data = data;
        }
        
    }

}
