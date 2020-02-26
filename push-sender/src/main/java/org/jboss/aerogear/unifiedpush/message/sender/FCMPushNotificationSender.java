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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.Priority;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.sender.fcm.ConfigurableFCMSender;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationAsyncService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.android.gcm.server.Message.Builder;
import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Notification;
import com.google.android.gcm.server.Result;

@Service
@Qualifier(value = VariantType.ANDROIDQ)
public class FCMPushNotificationSender implements PushNotificationSender {

    // collection of error codes we check for in the FCM response
    // in order to clean-up invalid or incorrect device tokens
    private static final Set<String> FCM_ERROR_CODES =
            new HashSet<>(Arrays.asList(
                    Constants.ERROR_INVALID_REGISTRATION,  // Bad registration_id.
                    Constants.ERROR_NOT_REGISTERED,        // The user has uninstalled the application or turned off notifications.
                    Constants.ERROR_MISMATCH_SENDER_ID)    // incorrect token, from a different project/sender ID
            );

    @Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private ClientInstallationAsyncService clientInstallationAsyncService;

    private final Logger logger = LoggerFactory.getLogger(FCMPushNotificationSender.class);

    /**
     * Sends FCM notifications ({@link UnifiedPushMessage}) to all devices, that are represented by
     * the {@link List} of tokens for the given {@link AndroidVariant}.
     */
    @Override
    public void sendPushMessage(Variant variant, Collection<String> tokens, UnifiedPushMessage pushMessage, String pushMessageInformationId, NotificationSenderCallback callback) {

        // no need to send empty list
        if (tokens.isEmpty()) {
            return;
        }

        final List<String> pushTargets = new ArrayList<>(tokens);
        final AndroidVariant androidVariant = (AndroidVariant) variant;

        // payload builder:
        Builder fcmBuilder = new Message.Builder();

        org.jboss.aerogear.unifiedpush.message.Message message = pushMessage.getMessage();
        // add the "recognized" keys...
        fcmBuilder.addData("alert", message.getAlert());
        fcmBuilder.addData("sound", message.getSound());
        fcmBuilder.addData("badge", String.valueOf(message.getBadge()));
        Notification notification = new Notification.Builder("").body(message.getAlert()).build();
        fcmBuilder.notification(notification);

        /*
        The Message defaults to a Normal priority.  High priority is used
        by FCM to wake up devices in Doze mode as well as apps in AppStandby
        mode.  This has no effect on devices older than Android 6.0
        */
        fcmBuilder.priority(
                message.getPriority() ==  Priority.HIGH ?
                                          Message.Priority.HIGH :
                                          Message.Priority.NORMAL
                           );

        // if present, apply the time-to-live metadata:
        int ttl = pushMessage.getConfig().getTimeToLive();
        if (ttl != -1) {
            fcmBuilder.timeToLive(ttl);
        }

        // iterate over the missing keys:
        message.getUserData().keySet()
                .forEach(key -> fcmBuilder.addData(key, String.valueOf(message.getUserData().get(key))));

        //add the aerogear-push-id
        fcmBuilder.addData(InternalUnifiedPushMessage.PUSH_MESSAGE_ID, pushMessageInformationId);

        Message fcmMessage = fcmBuilder.build();

        // send it out.....
        try {
            logger.debug("Sending transformed FCM payload: {}", fcmMessage);

            final ConfigurableFCMSender sender = new ConfigurableFCMSender(androidVariant.getGoogleKey());

            // send out a message to a batch of devices...
            processFCM(androidVariant, pushTargets, fcmMessage , sender);

            logger.debug("Message batch to FCM has been submitted");
            callback.onSuccess();

        } catch (Exception e) {
            // FCM exceptions:
            callback.onError(String.format("Error sending payload to FCM server: %s", e.getMessage()));
        }
    }

    /**
     * Process the HTTP POST to the FCM infrastructure for the given list of registrationIDs.
     */
    private void processFCM(AndroidVariant androidVariant, List<String> pushTargets, Message fcmMessage, ConfigurableFCMSender sender) throws IOException {


        // push targets can be registration IDs OR topics (starting /topic/), but they can't be mixed.
        if (pushTargets.get(0).startsWith(Constants.TOPIC_PREFIX)) {

            // perform the topic delivery

            for (String topic : pushTargets) {
                logger.info(String.format("Sent push notification to FCM topic: %s", topic));
                Result result = sender.sendNoRetry(fcmMessage, topic);

                logger.trace("Response from FCM topic request: {}", result);
            }
        } else {
            logger.info(String.format("Sent push notification to FCM Server for %d registrationIDs", pushTargets.size()));
            MulticastResult multicastResult = sender.sendNoRetry(fcmMessage, pushTargets);

            logger.trace("Response from FCM request: {}", multicastResult);

            // after sending, let's identify the inactive/invalid registrationIDs and trigger their deletion:
            cleanupInvalidRegistrationIDsForVariant(androidVariant.getVariantID(), multicastResult, pushTargets);
        }
    }

    /**
     * <p>Walks over the {@code MulticastResult} from the FCM call and identifies the <code>index</code> of all {@code Result} objects that
     * indicate an <code>InvalidRegistration</code> error.
     *
     * <p>This <code>index</code> is used to find the matching <code>registration ID</code> in the List of all used <code>registrationIDs</code>.
     *
     * <p>Afterwards all 'invalid' registration IDs for the given <code>variantID</code> are being deleted from our database.
     *
     * @param variantID id of the actual {@code AndroidVariantEntity}.
     * @param multicastResult the results from the HTTP request to the Google Cloud.
     * @param registrationIDs list of all tokens that we submitted to FCM.
     */
    private void cleanupInvalidRegistrationIDsForVariant(String variantID, MulticastResult multicastResult, List<String> registrationIDs) {

        // get the FCM send results for all of the client devices:
        final List<Result> results = multicastResult.getResults();

        // storage for all the invalid registration IDs:
        final Set<String> inactiveTokens = new HashSet<>();

        // read the results:
        for (int i = 0; i < results.size(); i++) {
            // use the current index to access the individual results
            final Result result = results.get(i);

            final String errorCodeName = result.getErrorCodeName();
            if (errorCodeName != null) {
                logger.info(String.format("Processing [%s] error code from FCM response, for registration ID: [%s]", errorCodeName, registrationIDs.get(i)));
            }

            //after sending, lets find tokens that are inactive from now on and need to be replaced with the new given canonical id.
            //according to fcm documentation, google refreshes tokens after some time. So the previous tokens will become invalid.
            //When you send a notification to a registration id which is expired, for the 1st time the message(notification) will be delivered
            //but you will get a new registration id with the name canonical id. Which mean, the registration id you sent the message to has
            //been changed to this canonical id, so change it on your server side as well.

            //check if current index of result has canonical id
            String canonicalRegId = result.getCanonicalRegistrationId();
            if (canonicalRegId != null) {
                // same device has more than one registration id: update it, if needed!
                // let's see if the canonical id is already in our system:
                Installation installation = clientInstallationService.findInstallationForVariantByDeviceToken(variantID, canonicalRegId);

                if (installation != null) {
                    // ok, there is already a device, with newest/latest registration ID (aka canonical id)
                    // It is time to remove the old reg id, to avoid duplicated messages in the future!
                    inactiveTokens.add(registrationIDs.get(i));

                } else {
                    // since there is no registered device with newest/latest registration ID (aka canonical id),
                    // this means the new token/regId was never stored on the server. Let's update the device and change its token to new canonical id:
                    installation = clientInstallationService.findInstallationForVariantByDeviceToken(variantID,registrationIDs.get(i));
                    installation.setDeviceToken(canonicalRegId);

                    //update installation with the new token
                    logger.info(String.format("Based on returned canonical id from FCM, updating Android installations with registration id [%s] with new token [%s] ", registrationIDs.get(i), canonicalRegId));
                    clientInstallationService.updateInstallation(installation);
                }

            } else {
                // is there any 'interesting' error code, which requires a clean up of the registration IDs
                if (FCM_ERROR_CODES.contains(errorCodeName)) {

                    // Ok the result at INDEX 'i' represents a 'bad' registrationID

                    // Now use the INDEX of the _that_ result object, and look
                    // for the matching registrationID inside of the List that contains
                    // _all_ the used registration IDs and store it:
                   inactiveTokens.add(registrationIDs.get(i));
                }
            }
        }

        if (! inactiveTokens.isEmpty()) {
            // trigger asynchronous deletion:
            logger.info(String.format("Based on FCM response data and error codes, deleting %d invalid or duplicated Android installations", inactiveTokens.size()));
            clientInstallationAsyncService.removeInstallationsForVariantByDeviceTokens(variantID, inactiveTokens);
        }
    }
}
