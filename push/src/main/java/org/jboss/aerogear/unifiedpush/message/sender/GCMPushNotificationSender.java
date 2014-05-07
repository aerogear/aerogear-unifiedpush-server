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
package org.jboss.aerogear.unifiedpush.message.sender;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.message.cache.GCMCache;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.android.gcm.server.Message.Builder;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

public class GCMPushNotificationSender {

    private final GCMCache cache = new GCMCache();

    @Inject
    private ClientInstallationService clientInstallationService;

    private final Logger logger = Logger.getLogger(GCMPushNotificationSender.class.getName());

    /**
     * Sends GCM notifications ({@link UnifiedPushMessage}) to all devices, that are represented by 
     * the {@link List} of tokens for the given {@link AndroidVariant}.
     * 
     * @param androidVariant The android variant entity
     * @param registrationIDs List of tokens, representing actual Android devices
     * @param pushMessage the payload to be submitted
     */
    public void sendPushMessage(AndroidVariant androidVariant, List<String> registrationIDs, UnifiedPushMessage pushMessage) {

        // no need to send empty list
        if (registrationIDs.isEmpty()) {
            return;
        }

        // payload builder:
        Builder gcmBuilder = new Message.Builder();

        // add the "recognized" keys...
        gcmBuilder.addData("alert", pushMessage.getAlert());
        gcmBuilder.addData("sound", pushMessage.getSound());
        gcmBuilder.addData("badge", "" + pushMessage.getBadge());

        // if present, apply the time-to-live metadata:
        int ttl = pushMessage.getTimeToLive();
        if (ttl != -1) {
            gcmBuilder.timeToLive(ttl);
        }

        // iterate over the missing keys:
        Set<String> keys = pushMessage.getData().keySet();
        for (String key : keys) {
            // GCM needs stringified values:
            gcmBuilder.addData(key, "" + pushMessage.getData().get(key));
        }

        Message gcmMessage = gcmBuilder.build();

        // send it out.....
        try {
            logger.log(Level.FINE, "Sending transformed GCM payload: " + gcmMessage);

            Sender sender = cache.getSenderForAPIKey(androidVariant.getGoogleKey());
            MulticastResult multicastResult = sender.send(gcmMessage, registrationIDs, 0);

            // after sending, let's identify the inactive/invalid registrationIDs and trigger their deletion:
            cleanupInvalidRegistrationIDsForVariant(androidVariant.getVariantID(), multicastResult, registrationIDs);

        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, "Error connection to your GCM project. Double check your Google API Key");
        } catch (Exception e) {
            // general GCM exceptions:
            logger.log(Level.SEVERE, "Error sending messages to GCM server", e);
        } finally {
            logger.log(Level.INFO, "Message to GCM has been submitted");
        }
    }

    /**
     * <p>Walks over the {@code MulticastResult} from the GCM call and identifies the <code>index</code> of all {@code Result} objects that
     * indicate an <code>InvalidRegistration</code> error.
     * 
     * <p>This <code>index</code> is used to find the matching <code>registration ID</code> in the List of all used <code>registrationIDs</code>.
     * 
     * <p>Afterwards all 'invalid' registration IDs for the given <code>variantID</code> are being deleted from our database.
     * 
     * @param variantID id of the actual {@code AndroidVariantEntity}.
     * @param multicastResult the results from the HTTP request to the Google Cloud.
     * @param registrationIDs list of all tokens that we submitted to GCM.
     */
    private void cleanupInvalidRegistrationIDsForVariant(String variantID, MulticastResult multicastResult, List<String> registrationIDs) {

        // get the GCM send results for all of the client devices:
        final List<Result> results = multicastResult.getResults();

        // storage for all the invalid registration IDs:
        final Set<String> inactiveTokens = new HashSet<String>();

        // read the results:
        for (int i = 0; i < results.size(); i++) {
            // use the current index to access the individual results
            Result result = results.get(i);

            // is there an error code that indicates an invalid regID ?
            if (Constants.ERROR_INVALID_REGISTRATION.equals(result.getErrorCodeName())) {

                // Ok the result at INDEX 'i' was an 'InvalidRegistration'!

                // Now use the INDEX of the 'InvalidRegistration' result object, and look
                // for the matching registrationID inside of the List that contains
                // all the used registration IDs and store it:
                inactiveTokens.add(registrationIDs.get(i));
            }
        }

        // trigger asynchronous deletion:
        logger.log(Level.FINE, "Deleting '" + inactiveTokens.size() + "' invalid Android installations");
        clientInstallationService.removeInstallationsForVariantByDeviceTokens(variantID, inactiveTokens);
    }
}
