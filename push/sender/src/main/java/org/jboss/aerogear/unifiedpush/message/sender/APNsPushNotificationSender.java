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

import static org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils.tryGetIntegerProperty;
import static org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils.tryGetProperty;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.apns.APNs;
import org.jboss.aerogear.unifiedpush.message.cache.AbstractServiceCache.ServiceConstructor;
import org.jboss.aerogear.unifiedpush.message.cache.ApnsServiceCache;
import org.jboss.aerogear.unifiedpush.message.exception.PushNetworkUnreachableException;
import org.jboss.aerogear.unifiedpush.message.exception.SenderResourceNotAvailableException;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

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

@SenderType(VariantType.IOS)
public class APNsPushNotificationSender implements PushNotificationSender {

    public static final String CUSTOM_AEROGEAR_APNS_PUSH_HOST = "custom.aerogear.apns.push.host";
    public static final String CUSTOM_AEROGEAR_APNS_PUSH_PORT = "custom.aerogear.apns.push.port";
    private static final String CUSTOM_AEROGEAR_APNS_FEEDBACK_HOST = "custom.aerogear.apns.feedback.host";
    private static final String CUSTOM_AEROGEAR_APNS_FEEDBACK_PORT = "custom.aerogear.apns.feedback.port";

    private static final String customAerogearApnsPushHost = tryGetProperty(CUSTOM_AEROGEAR_APNS_PUSH_HOST);
    private static final Integer customAerogearApnsPushPort = tryGetIntegerProperty(CUSTOM_AEROGEAR_APNS_PUSH_PORT);
    private static final String customAerogearApnsFeedbackHost = tryGetProperty(CUSTOM_AEROGEAR_APNS_FEEDBACK_HOST);
    private static final Integer customAerogearApnsFeedbackPort  = tryGetIntegerProperty(CUSTOM_AEROGEAR_APNS_FEEDBACK_PORT);

    private final AeroGearLogger logger = AeroGearLogger.getInstance(APNsPushNotificationSender.class);

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    private ApnsServiceCache apnsServiceCache;

    public APNsPushNotificationSender() {
    }

    /**
     * Constructor used for test purposes
     */
    APNsPushNotificationSender(ApnsServiceCache apnsServiceCache) {
        this.apnsServiceCache = apnsServiceCache;
    }

    /**
     * Sends APNs notifications ({@link UnifiedPushMessage}) to all devices, that are represented by
     * the {@link Collection} of tokens for the given {@link iOSVariant}.
     *
     * @param variant contains details for the underlying push network, e.g. API Keys/Ids
     * @param tokens contains the list of tokens that identifies the installation to which the message will be sent
     * @param pushMessage payload to be send to the given clients
     * @param pushMessageInformationId the id of the PushMessageInformation instance associated with this send.
     * @param callback that will be invoked after the sending.
     */
    public void sendPushMessage(final Variant variant, final Collection<String> tokens, final UnifiedPushMessage pushMessage, final String pushMessageInformationId, final NotificationSenderCallback callback) {
        // no need to send empty list
        if (tokens.isEmpty()) {
            return;
        }
        final iOSVariant iOSVariant = (iOSVariant) variant;

        Message message = pushMessage.getMessage();
        APNs apns = message.getApns();
        PayloadBuilder builder = APNS.newPayload()
                // adding recognized key values
                .alertBody(message.getAlert()) // alert dialog, in iOS or Safari
                .sound(message.getSound()) // sound to be played by app
                .alertTitle(apns.getTitle()) // The title of the notification in Safari and Apple Watch
                .alertAction(apns.getAction()) // The label of the action button, if the user sets the notifications to appear as alerts in Safari.
                .urlArgs(apns.getUrlArgs())
                .category(apns.getActionCategory()) // iOS8: User Action category
                .localizedTitleKey(apns.getLocalizedTitleKey()); //iOS8 : Localized Title Key

        // was a badge included?
        if (message.getBadge() >= 0) {
            builder.badge(message.getBadge()); // only set badge if needed
        }

        //this kind of check should belong in java-apns
        if(apns.getLocalizedTitleArguments() != null) {
            builder .localizedArguments(apns.getLocalizedTitleArguments()); //iOS8 : Localized Title Arguments;
        }

       // apply the 'content-available:1' value:
        if (apns.isContentAvailable()) {
            // content-available is for 'silent' notifications and Newsstand
            builder = builder.instantDeliveryOrSilentNotification();
        }

        builder = builder.customFields(message.getUserData()); // adding other (submitted) fields

        //add aerogear-push-id
        builder = builder.customField(InternalUnifiedPushMessage.PUSH_MESSAGE_ID, pushMessageInformationId);

        // we are done with adding values here, before building let's check if the msg is too long
        if (builder.isTooLong()) {
            // invoke the error callback and return, as it is pointless to send something out
            callback.onError("Nothing sent to APNs since the payload is too large");
            return;
        }

        // all good, let's build the JSON payload for APNs
        final String apnsMessage  =  builder.build();

        ApnsService service = apnsServiceCache.dequeueOrCreateNewService(pushMessageInformationId, iOSVariant.getVariantID(), new ServiceConstructor<ApnsService>() {
            @Override
            public ApnsService construct() {
                ApnsService service = buildApnsService(iOSVariant, callback);
                if (service == null) {
                    callback.onError("No certificate was found. Could not send messages to APNs");
                    throw new IllegalStateException("No certificate was found. Could not send messages to APNs");
                } else {
                    logger.fine("Starting APNs service");
                    try {
                        service.start();
                    } catch (Exception e) {
                        throw new PushNetworkUnreachableException(e);
                    }
                    return service;
                }
            }
        });
        if (service == null) {
            throw new SenderResourceNotAvailableException("Unable to obtain a ApnsService instance");
        }
        try {
            logger.fine("Sending transformed APNs payload: " + apnsMessage);
            Date expireDate = createFutureDateBasedOnTTL(pushMessage.getConfig().getTimeToLive());
            service.push(tokens, apnsMessage, expireDate);

            logger.info("One batch to APNs has been submitted");
            apnsServiceCache.queueFreedUpService(pushMessageInformationId, iOSVariant.getVariantID(), service);
            try {
                service = null; // we don't want a failure in onSuccess stop the APNs service
                callback.onSuccess();
            } catch (Exception e) {
                logger.severe("Failed to call onSuccess after successful push", e);
            }
        } catch (Exception e) {
            try {
                logger.warning("APNs service died in the middle of sending, stopping it");
                try {
                    service.stop();
                } catch (Exception ex) {
                    logger.severe("Failed to stop the APNs service after failure", ex);
                }
                callback.onError("Error sending payload to APNs server: " + e.getMessage());
            } finally {
                apnsServiceCache.freeUpSlot(pushMessageInformationId, iOSVariant.getVariantID());
            }
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