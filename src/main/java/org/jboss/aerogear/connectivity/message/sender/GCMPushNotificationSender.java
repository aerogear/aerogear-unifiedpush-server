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

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.aerogear.connectivity.message.cache.GCMCache;
import org.jboss.aerogear.connectivity.service.sender.message.UnifiedPushMessage;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Sender;
import com.google.android.gcm.server.Message.Builder;

@ApplicationScoped
public class GCMPushNotificationSender {

    @Inject
    private GCMCache cache;

    @Inject
    private Logger logger;

    /**
     * Sends GCM notifications ({@link UnifiedPushMessage}) to all devices, that are represented by 
     * the {@link Collection} of tokens for the given Google API key
     * 
     * @param tokens collection of tokens, representing actual Android devices
     * @param pushMessage the payload to be submitted
     * @param apiKey the Google API key
     */
    public void sendPushMessage(Collection<String> tokens, UnifiedPushMessage pushMessage, String apiKey) {

        // no need to send empty list
        if (tokens.isEmpty())
            return;

        // payload builder:
        Builder gcmBuilder = new Message.Builder();

        // add the "recognized" keys...
        gcmBuilder.addData("alert", pushMessage.getAlert());
        gcmBuilder.addData("sound", pushMessage.getSound());
        gcmBuilder.addData("badge", "" + pushMessage.getBadge());

        // iterate over the missing keys:
        Set<String> keys = pushMessage.getData().keySet();
        for (String key : keys) {
            // GCM needs stringified values:
            gcmBuilder.addData(key, "" + pushMessage.getData().get(key));
        }

        Message gcmMessage = gcmBuilder.build();

        // send it out.....
        try {
            logger.fine(String.format("Sending transformed GCM payload: '%s' ", gcmMessage));

            Sender sender = cache.getSenderForAPIKey(apiKey);
            sender.send(gcmMessage, (List<String>) tokens, 0);
        } catch (IOException e) {
            // network related exceptions:
            logger.warning("Error sending messages to GCM server");
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            logger.severe("Error connection to your GCM project. Double check your Google API Key");
        }
    }
}
