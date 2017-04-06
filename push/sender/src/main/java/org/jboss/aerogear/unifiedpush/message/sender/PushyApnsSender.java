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

import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ApnsClientBuilder;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.apns.APNs;
import org.jboss.aerogear.unifiedpush.message.cache.ServiceConstructor;
import org.jboss.aerogear.unifiedpush.message.cache.SimpleApnsClientCache;
import org.jboss.aerogear.unifiedpush.message.sender.apns.AeroGearApnsPushNotification;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils.tryGetIntegerProperty;
import static org.jboss.aerogear.unifiedpush.message.util.ConfigurationUtils.tryGetProperty;

@Stateless
@SenderType(VariantType.IOS)
public class PushyApnsSender implements PushNotificationSender {

    private final Logger logger = LoggerFactory.getLogger(PushyApnsSender.class);

    public static final String CUSTOM_AEROGEAR_APNS_PUSH_HOST = "custom.aerogear.apns.push.host";
    public static final String CUSTOM_AEROGEAR_APNS_PUSH_PORT = "custom.aerogear.apns.push.port";
    private static final String customAerogearApnsPushHost = tryGetProperty(CUSTOM_AEROGEAR_APNS_PUSH_HOST);
    private static final Integer customAerogearApnsPushPort = tryGetIntegerProperty(CUSTOM_AEROGEAR_APNS_PUSH_PORT);

    private final ConcurrentSkipListSet<String> invalidTokens = new ConcurrentSkipListSet();

    @Inject
    private SimpleApnsClientCache simpleApnsClientCache;
    @Inject
    private ClientInstallationService clientInstallationService;


    @Override
    public void sendPushMessage(final Variant variant, final Collection<String> tokens, final UnifiedPushMessage pushMessage, final String pushMessageInformationId, final NotificationSenderCallback senderCallback) {
        // no need to send empty list
        if (tokens.isEmpty()) {
            return;
        }

        final iOSVariant iOSVariant = (iOSVariant) variant;

        final String payload;
        {
            try {
                payload = createPushPayload(pushMessage.getMessage(), pushMessageInformationId);
            } catch (IllegalArgumentException iae) {
                logger.info(iae.getMessage(), iae);
                senderCallback.onError("Nothing sent to APNs since the payload is too large");
                return;
            }
        }

        final ApnsClient apnsClient;
        {
            try {
                apnsClient = receiveApnsConnection(iOSVariant);
            } catch (IllegalArgumentException iae) {
                logger.error(iae.getMessage(), iae);
                senderCallback.onError(String.format("Unable to connect to APNs (%s))", iae.getMessage()));
                return;
            }
        }

        if (apnsClient.isConnected()) {
            logger.debug(String.format("sending payload for all tokens for '%s' to APNs", iOSVariant.getVariantID()));
            for (final String token : tokens) {

                final AeroGearApnsPushNotification pushNotification = new AeroGearApnsPushNotification(token, payload);
                final Future<PushNotificationResponse<AeroGearApnsPushNotification>> notificationSendFuture = apnsClient.sendNotification(pushNotification);

                notificationSendFuture.addListener(new GenericFutureListener<Future<? super PushNotificationResponse<AeroGearApnsPushNotification>>>() {
                    @Override
                    public void operationComplete(Future<? super PushNotificationResponse<AeroGearApnsPushNotification>> future) throws Exception {

                        if (future.isSuccess()) {
                            handlePushNotificationResponsePerToken(notificationSendFuture.get());
                        }
                    }
                });
            }

            // we have managed to dispatch all messages ;-)
            senderCallback.onSuccess();
        } else {
            logger.error("Unable to send notifications, client is not connected");
            senderCallback.onError("Unable to send notifications, client is not connected");
        }
    }

    private void handlePushNotificationResponsePerToken(final PushNotificationResponse<AeroGearApnsPushNotification> pushNotificationResponse ) {

        final String deviceToken = pushNotificationResponse.getPushNotification().getToken();

        if (pushNotificationResponse.isAccepted()) {
            logger.trace(String.format("Push notification for '%s' (payload=%s)", deviceToken, pushNotificationResponse.getPushNotification().getPayload()));
        } else {
            final String rejectReason = pushNotificationResponse.getRejectionReason();

            // token is either invalid, or did just expire
            if ((pushNotificationResponse.getTokenInvalidationTimestamp() != null) || ("BadDeviceToken".equals(rejectReason))) {
                logger.info(rejectReason + ", removing token: " + deviceToken);

                invalidTokens.add(deviceToken);
            }
        }
    }

    private String createPushPayload(final Message message, final String pushMessageInformationId) {
        final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
        final APNs apns = message.getApns();


        // only set badge if needed/included in user's payload
        if (message.getBadge() >= 0) {
            payloadBuilder.setBadgeNumber(message.getBadge());
        }

        payloadBuilder
                .addCustomProperty(InternalUnifiedPushMessage.PUSH_MESSAGE_ID, pushMessageInformationId)
                .setAlertBody(message.getAlert())
                //.setLocalizedAlertMessage(apns.getLocalizedKey(), apns.getLocalizedArguments())
                .setSoundFileName(message.getSound())
                .setAlertTitle(apns.getTitle())
                //.setLocalizedAlertTitle(apns.getLocalizedTitleKey(), apns.getLocalizedTitleArguments())
                .setActionButtonLabel(apns.getAction())
                .setUrlArguments(apns.getUrlArgs())
                .setCategoryName(apns.getActionCategory())
                .setContentAvailable(apns.isContentAvailable());

        // custom fields
        final Map<String, Object> userData = message.getUserData();
        for (Map.Entry<String, Object> entry : userData.entrySet()) {
            payloadBuilder.addCustomProperty(entry.getKey(), entry.getValue());
        }

        return payloadBuilder.buildWithDefaultMaximumLength();
    }


    private ApnsClient receiveApnsConnection(final iOSVariant iOSVariant) {
        return simpleApnsClientCache.getApnsClientForVariantID(iOSVariant.getVariantID(), new ServiceConstructor<ApnsClient>() {
            @Override
            public ApnsClient construct() {
                final ApnsClient apnsClient = buildApnsClient(iOSVariant);

                // connect and wait:
                logger.debug(String.format("establishing the connection for %s", iOSVariant.getVariantID()));
                connectToDestinations(iOSVariant, apnsClient);

                // APNS client has auto-reconnect, but let's log when that happens
                apnsClient.getReconnectionFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        logger.debug("Reconnecting to APNs");
                    }
                });
                return apnsClient;
            }
        });
    }

    private ApnsClient buildApnsClient(final iOSVariant iOSVariant) {

        // this check should not be needed, but you never know:
        if (iOSVariant.getCertificate() != null && iOSVariant.getPassphrase() != null) {

            // add the certificate:
            try {
                final ByteArrayInputStream stream = new ByteArrayInputStream(iOSVariant.getCertificate());

                final ApnsClient apnsClient = new ApnsClientBuilder()
                        .setClientCredentials(stream, iOSVariant.getPassphrase())
                        .build();

                // release the stream
                stream.close();

                return apnsClient;
            } catch (Exception e) {
                logger.error("Error reading certificate", e);
                // will be thrown below
            }
        }
        // indicating an incomplete service
        throw new IllegalArgumentException("Not able to construct APNS client");
    }

    private synchronized void connectToDestinations(final iOSVariant iOSVariant, final ApnsClient apnsClient) {

        String apnsHost;
        int apnsPort = ApnsClient.DEFAULT_APNS_PORT;

        // are we production or development ?
        if (iOSVariant.isProduction()) {
            apnsHost = ApnsClient.PRODUCTION_APNS_HOST;
        } else {
            apnsHost = ApnsClient.DEVELOPMENT_APNS_HOST;
        }

        //Or is there even a custom ost&port provided by a system property, for tests ?
        if(customAerogearApnsPushHost != null){
            apnsHost = customAerogearApnsPushHost;

            if(customAerogearApnsPushPort != null) {
                apnsPort = customAerogearApnsPushPort;
            }
        }

        // Once we've created a client, we can connect it to the APNs gateway.
        // Note that this process is asynchronous; we'll get a Future right
        // away, but we'll need to wait for it to complete before we can send
        // any notifications. Note that this is a Netty Future, which is an
        // extension of the Java Future interface that allows callers to add
        // listeners and adds methods for checking the status of the Future.
        logger.debug("connecting to APNs");
        final Future<Void> connectFuture = apnsClient.connect(apnsHost, apnsPort);
        try {
            connectFuture.await();
        } catch (InterruptedException e) {
            logger.error("Error connecting to APNs", e);
        }
    }
}
