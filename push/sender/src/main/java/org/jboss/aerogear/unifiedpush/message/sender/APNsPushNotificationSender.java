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

import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegateAdapter;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.notnoop.apns.DeliveryError;
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.PayloadBuilder;
import com.notnoop.apns.internal.Utilities;
import com.notnoop.exceptions.ApnsDeliveryErrorException;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import static org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils.tryGetProperty;
import static org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils.tryGetIntegerProperty;

@SenderType(iOSVariant.class)
public class APNsPushNotificationSender implements PushNotificationSender {

    private static final String CUSTOM_AEROGEAR_APNS_PUSH_HOST = "custom.aerogear.apns.push.host";
    private static final String CUSTOM_AEROGEAR_APNS_PUSH_PORT = "custom.aerogear.apns.push.port";
    private static final String CUSTOM_AEROGEAR_APNS_FEEDBACK_HOST = "custom.aerogear.apns.feedback.host";
    private static final String CUSTOM_AEROGEAR_APNS_FEEDBACK_PORT = "custom.aerogear.apns.feedback.port";
    
    private static final String customAerogearApnsPushHost = tryGetProperty(CUSTOM_AEROGEAR_APNS_PUSH_HOST);
    private static final Integer customAerogearApnsPushPort = tryGetIntegerProperty(CUSTOM_AEROGEAR_APNS_PUSH_PORT);
    private static final String customAerogearApnsFeedbackHost = tryGetProperty(CUSTOM_AEROGEAR_APNS_FEEDBACK_HOST);
    private static final Integer customAerogearApnsFeedbackPort  = tryGetIntegerProperty(CUSTOM_AEROGEAR_APNS_FEEDBACK_PORT);

    private final AeroGearLogger logger = AeroGearLogger.getInstance(APNsPushNotificationSender.class);

    @Inject
    private ClientInstallationService clientInstallationService;

    /**
     * Sends APNs notifications ({@link UnifiedPushMessage}) to all devices, that are represented by
     * the {@link Collection} of tokens for the given {@link iOSVariant}.
     */
    public void sendPushMessage(final Variant variant, final Collection<String> tokens, final UnifiedPushMessage pushMessage, final NotificationSenderCallback callback) {
        // no need to send empty list
        if (tokens.isEmpty()) {
            return;
        }

        final iOSVariant iOSVariant = (iOSVariant) variant;

        Message message = pushMessage.getMessage();
        PayloadBuilder builder = APNS.newPayload()
                // adding recognized key values
                .alertBody(message.getAlert()) // alert dialog, in iOS or Safari
                .alertTitle(message.getTitle()) // The title of the notification in Safari
                .alertAction(message.getAction()) // The label of the action button, if the user sets the notifications to appear as alerts in Safari.
                .urlArgs(message.getUrlArgs())
                .badge(message.getBadge()) // little badge icon update;
                .sound(message.getSound()) // sound to be played by app
                .category(message.getActionCategory()); // iOS8: User Action category

                // apply the 'content-available:1' value:
                if (message.isContentAvailable()) {
                    // content-available is for 'silent' notifications and Newsstand
                    builder = builder.instantDeliveryOrSilentNotification();
                }

                builder = builder.customFields(message.getUserData()); // adding other (submitted) fields

        // we are done with adding values here, before building let's check if the msg is too long
        if (builder.isTooLong()) {
            // invoke the error callback and return, as it is pointless to send something out
            callback.onError("Nothing sent to APNs since the payload is too large");
            return;
        }

        // all good, let's build the JSON payload for APNs
        final String apnsMessage  =  builder.build();

        ApnsService service = buildApnsService(iOSVariant, callback);

        if (service != null) {
            try {
                logger.fine("Sending transformed APNs payload: " + apnsMessage);
                // send:
                service.start();

                Date expireDate = createFutureDateBasedOnTTL(pushMessage.getConfig().getTimeToLive());
                service.push(tokens, apnsMessage, expireDate);
                logger.info("Message to APNs has been submitted");

                // after sending, let's ask for the inactive tokens:
                final Set<String> inactiveTokens = service.getInactiveDevices().keySet();
                // transform the tokens to be all lower-case:
                final Set<String> transformedTokens = lowerCaseAllTokens(inactiveTokens);

                // trigger asynchronous deletion:
                if (! transformedTokens.isEmpty()) {
                    logger.info("Deleting '" + inactiveTokens.size() + "' invalid iOS installations");
                    clientInstallationService.removeInstallationsForVariantByDeviceTokens(iOSVariant.getVariantID(), transformedTokens);
                }
                callback.onSuccess();
            } catch (Exception e) {
                callback.onError("Error sending payload to APNs server: " + e.getMessage());
            } finally {
                // tear down and release resources:
                service.stop();
            }
        } else {
            callback.onError("No certificate was found. Could not send messages to APNs");
        }
    }

    /**
     * Helper method that creates a future {@link Date}, based on the given ttl/time-to-live value.
     * If no TTL was provided, we use the max date from the APNs library
     */
    private Date createFutureDateBasedOnTTL(int ttl) {

        // no TTL was specified on the payload, we use the MAX Default from the APNs library:
        if (ttl == -1) {
            return new Date(System.currentTimeMillis() + EnhancedApnsNotification.MAXIMUM_EXPIRY * 1000L);
        } else {
            // apply the given TTL to the current time
            return new Date(System.currentTimeMillis() + ttl);
        }
    }

    /**
     * The Java-APNs lib returns the tokens in UPPERCASE format, however, the iOS Devices submit the token in
     * LOWER CASE format. This helper method performs a transformation
     */
    private Set<String> lowerCaseAllTokens(Set<String> inactiveTokens) {
        final Set<String> lowerCaseTokens = new HashSet<String>();
        for (String token : inactiveTokens) {
            lowerCaseTokens.add(token.toLowerCase());
        }
        return lowerCaseTokens;
    }

    /**
     * Returns the ApnsService, based on the required profile (production VS sandbox/test).
     * Null is returned if there is no "configuration" for the request stage
     */
    private ApnsService buildApnsService(final iOSVariant iOSVariant, final NotificationSenderCallback notificationSenderCallback) {

        // this check should not be needed, but you never know:
        if (iOSVariant.getCertificate() != null && iOSVariant.getPassphrase() != null) {

            final ApnsServiceBuilder builder = APNS.newService();

            // using the APNS Delegate callback to log success/failure for each token:
            builder.withDelegate(new ApnsDelegateAdapter() {
                @Override
                public void messageSent(ApnsNotification message, boolean resent) {
                    // Invoked for EVERY devicetoken:
                    logger.finest("Sending APNs message to: " + message.getDeviceToken());
                }

                @Override
                public void messageSendFailed(ApnsNotification message, Throwable e) {
                    if (e.getClass().isAssignableFrom(ApnsDeliveryErrorException.class)) {
                        ApnsDeliveryErrorException deliveryError = (ApnsDeliveryErrorException) e;
                        if (DeliveryError.INVALID_TOKEN.equals(deliveryError.getDeliveryError())) {
                            final String invalidToken = Utilities.encodeHex(message.getDeviceToken()).toLowerCase();
                            logger.info("Removing invalid token: " + invalidToken);
                            clientInstallationService.removeInstallationForVariantByDeviceToken(iOSVariant.getVariantID(), invalidToken);
                        } else {
                            // for now, we just log the other cases
                            logger.severe("Error sending payload to APNs server", e);
                        }
                    }
                }
            });

            // add the certificate:
            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(iOSVariant.getCertificate());
                builder.withCert(stream, iOSVariant.getPassphrase());

                // release the stream
                stream.close();
            } catch (Exception e) {
                logger.severe("Error reading certificate", e);

                // indicating an incomplete service
                return null;
            }

            configureDestinations(iOSVariant, builder);


            // create the service
            return builder.build();
        }
        // null if, why ever, there was no cert/passphrase
        return null;
    }

    /**
     * Configure the Gateway to the Apns servers.
     * Default gateway and port can be override with respectively :
     *  - custom.aerogear.apns.push.host
     *  - custom.aerogear.apns.push.port
     *
     * Feedback gateway and port can be override with  respectively :
     *  - custom.aerogear.apns.feedback.host
     *  - custom.aerogear.apns.feedback.port
     * @param iOSVariant
     * @param builder
     */
    private void configureDestinations(iOSVariant iOSVariant, ApnsServiceBuilder builder) {
        // pick the destination, based on submitted profile:
        builder.withAppleDestination(iOSVariant.isProduction());

        //Is the gateway host&port provided by a system property, for tests ?
        if(customAerogearApnsPushHost != null){
            int port = Utilities.SANDBOX_GATEWAY_PORT;
            if(customAerogearApnsPushPort != null) {
                port = customAerogearApnsPushPort;
            }
            builder.withGatewayDestination(customAerogearApnsPushHost, port);
        }

        //Is the feedback gateway provided by a system property, for tests ?
        if(customAerogearApnsFeedbackHost != null){
            int port = Utilities.SANDBOX_FEEDBACK_PORT;
            if(customAerogearApnsFeedbackPort != null) {
                port = customAerogearApnsFeedbackPort;
            }
            builder.withFeedbackDestination(customAerogearApnsFeedbackHost, port);
        }
    }
}