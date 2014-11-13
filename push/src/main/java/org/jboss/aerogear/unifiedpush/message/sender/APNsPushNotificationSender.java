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
import com.notnoop.apns.EnhancedApnsNotification;
import com.notnoop.apns.PayloadBuilder;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@SenderType(iOSVariant.class)
public class APNsPushNotificationSender implements PushNotificationSender {

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

        PayloadBuilder builder = APNS.newPayload()
                // adding recognized key values
                .alertBody(pushMessage.getAlert()) // alert dialog, in iOS
                .badge(pushMessage.getBadge()) // little badge icon update;
                .sound(pushMessage.getSound()) // sound to be played by app
                .category(pushMessage.getActionCategory()); // iOS8: User Action category

                // apply the 'content-available:1' value:
                if (pushMessage.isContentAvailable()) {
                    // content-available is for 'silent' notifications and Newsstand
                    builder = builder.instantDeliveryOrSilentNotification();
                }

                builder = builder.customFields(pushMessage.getData()); // adding other (submitted) fields

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

                Date expireDate = createFutureDateBasedOnTTL(pushMessage.getTimeToLive());
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
    private ApnsService buildApnsService(iOSVariant iOSVariant, final NotificationSenderCallback notificationSenderCallback) {

        // this check should not be needed, but you never know:
        if (iOSVariant.getCertificate() != null && iOSVariant.getPassphrase() != null) {

            final ApnsServiceBuilder builder = APNS.newService().withNoErrorDetection();

            // using the APNS Delegate callback to log success/failure for each token:
            builder.withDelegate(new ApnsDelegateAdapter() {
                @Override
                public void messageSent(ApnsNotification message, boolean resent) {
                    logger.fine("Sending APNs message: " + message.getDeviceToken());
                }

                @Override
                public void messageSendFailed(ApnsNotification message, Throwable e) {
                    logger.severe("Error sending payload to APNs server", e);
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

            // pick the destination:
            if (iOSVariant.isProduction()) {
                builder.withProductionDestination();
            } else {
                builder.withSandboxDestination();
            }

            // create the service
            return builder.build();
        }
        // null if, why ever, there was no cert/passphrase
        return null;
    }
}