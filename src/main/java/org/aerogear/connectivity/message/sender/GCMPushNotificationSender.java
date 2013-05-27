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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.aerogear.connectivity.message.sender.annotations.GCMSender;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.android.gcm.server.Message.Builder;

@GCMSender
public class GCMPushNotificationSender implements PushNotificationSender {
    
    private final Sender sender;
    
    public GCMPushNotificationSender(String googleAPIKey) {
        sender = new Sender(googleAPIKey);
    }

    @Override
    public void sendPushMessage(Collection<String> tokens, UnifiedPushMessage pushMessage) {
        // payload builder:
        Builder gcmBuilder = new Message.Builder();
        
        // add the "regconized" keys...
        gcmBuilder.addData("alert", pushMessage.getAlert());
        gcmBuilder.addData("sound", pushMessage.getSound());
        gcmBuilder.addData("badge", ""+pushMessage.getBadge());
        
        // iterate over the missing keys:
        Set<String> keys = pushMessage.getData().keySet();
        for (String key : keys) {
            gcmBuilder.addData(key, (String) pushMessage.getData().get(key));
        }

        Message gcmMessage = gcmBuilder.build();

        // send it out.....
        try {
            sender.send(gcmMessage, (List<String>) tokens, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
