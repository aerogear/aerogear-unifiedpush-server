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

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.cache.GCMCache;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@SenderType(AndroidVariant.class)
public class GCMPushNotificationSender implements PushNotificationSender {

    private final GCMCache cache = new GCMCache();
    private static final int GCM_PAGE = 1000;

    @Inject
    private ClientInstallationService clientInstallationService;

    private final Logger logger = Logger.getLogger(GCMPushNotificationSender.class.getName());

    /**
     * Sends GCM notifications ({@link UnifiedPushMessage}) to all devices, that are represented by 
     * the {@link List} of tokens for the given {@link AndroidVariant}.
     */
    public void sendPushMessage(Variant variant, Collection<String> tokens, UnifiedPushMessage pushMessage, NotificationSenderCallback callback) {

        // no need to send empty list
        if (tokens.isEmpty()) {
            return;
        }

        List<String>  registrationIDs = (List<String>) tokens;
        final AndroidVariant androidVariant = (AndroidVariant) variant;

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


            // GCM does only allow a 1000 device IDs
            while (! registrationIDs.isEmpty()) {

                int toIndex = GCM_PAGE;

                if (registrationIDs.size() < GCM_PAGE) {
                    toIndex = registrationIDs.size();
                }
                List<String> sublist = registrationIDs.subList(0, toIndex);

                // send out a message to a few devices...
                processGCM(androidVariant, sublist, gcmMessage, sender);
                registrationIDs.removeAll(sublist);
            }

            logger.log(Level.INFO, "Message to GCM has been submitted");
            callback.onSuccess();

        } catch (Exception e) {
            // GCM exceptions:
            logger.log(Level.SEVERE, "Error sending payload to GCM server", e);
            callback.onError("Error sending payload to GCM server");
        }
    }

    /**
     * Process the HTTP POST to the GCM infrastructor for the given list of registrationIDs.     *
     */
    private void processGCM(AndroidVariant androidVariant, List<String> registrationIDs, Message gcmMessage, Sender sender) throws IOException {

        logger.log(Level.INFO, "Sending payload for [" + registrationIDs.size() + "] devices to GCM");

        MulticastResult multicastResult = sender.send(gcmMessage, registrationIDs, 0);

        // after sending, let's identify the inactive/invalid registrationIDs and trigger their deletion:
        cleanupInvalidRegistrationIDsForVariant(androidVariant.getVariantID(), multicastResult, registrationIDs);
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
