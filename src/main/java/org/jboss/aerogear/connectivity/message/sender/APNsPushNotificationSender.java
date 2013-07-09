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
    APNsCache apnsCache;

    public void sendPushMessage(iOSVariant iOSVariant, Collection<String> tokens, UnifiedPushMessage pushMessage) {

        String apnsMessage = APNS.newPayload()
                    // adding recognized key values
                .alertBody(pushMessage.getAlert()) // alert dialog, in iOS
                .badge(pushMessage.getBadge()) // little badge icon update;
                .sound(pushMessage.getSound()) // sound to be played by app

                .customFields(pushMessage.getData()) // adding other (submitted) fields

                .build(); // build the JSON payload, for APNs 

        // look up the ApnsService from the cache:
        ApnsService service = apnsCache.getApnsServiceForVariant(iOSVariant);

        // send: 
        service.push(tokens, apnsMessage);
    }
}
