package org.aerogear.push.iosserver.rest.org.aerogear.push.sender.ios;

/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.turo.pushy.apns.ApnsClient;
import com.turo.pushy.apns.ApnsClientBuilder;
import com.turo.pushy.apns.PushNotificationResponse;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import io.netty.util.concurrent.Future;
import org.aerogear.kafka.SimpleKafkaProducer;
import org.aerogear.kafka.cdi.annotation.Producer;
import org.aerogear.push.iosserver.rest.helper.InternalUnifiedPushMessage;
import org.aerogear.push.iosserver.rest.org.aerogear.push.sender.ios.cache.SimpleApnsClientCache;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.event.iOSVariantUpdateEvent;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.apns.APNs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;

public class PushyApnsSender {

    private final Logger logger = LoggerFactory.getLogger(PushyApnsSender.class);

    public static final String KAFKA_INVALID_TOKEN_TOPIC = "agpush_invalidToken";


    /**
     * Topic to which a "success" message will be sent if a token was accepted and "failure" message otherwise.
     */
    public static final String KAFKA_APNS_TOKEN_DELIVERY_METRICS_TOPIC = "agpush_apnsTokenDeliveryMetrics";
    public static final String KAFKA_APNS_TOKEN_DELIVERY_SUCCESS = "agpush_apnsTokenDeliverySuccess";
    public static final String KAFKA_APNS_TOKEN_DELIVERY_FAILURE = "agpush_pnsTokenDeliveryFailure";

    public static final String CUSTOM_AEROGEAR_APNS_PUSH_HOST = "custom.aerogear.apns.push.host";
    public static final String CUSTOM_AEROGEAR_APNS_PUSH_PORT = "custom.aerogear.apns.push.port";
//    private static final String customAerogearApnsPushHost = tryGetProperty(CUSTOM_AEROGEAR_APNS_PUSH_HOST);
//    private static final Integer customAerogearApnsPushPort = tryGetIntegerProperty(CUSTOM_AEROGEAR_APNS_PUSH_PORT);

    @Inject
    private SimpleApnsClientCache simpleApnsClientCache;

    @Inject
    private Event<iOSVariantUpdateEvent> variantUpdateEventEvent;

    @Producer
    private SimpleKafkaProducer<String, String> invalidTokenProducer;


    @Producer
    private SimpleKafkaProducer<String, String> tokenDeliveryMetricsProducer;


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

            // we have managed to connect and will send tokens ;-)
            senderCallback.onSuccess();

            final String defaultApnsTopic = ApnsUtil.readDefaultTopic(iOSVariant.getCertificate(), iOSVariant.getPassphrase().toCharArray());
            logger.error("sending payload for all tokens for {} to APNs ({})", iOSVariant.getVariantID(), defaultApnsTopic);


            tokens.forEach(token -> {
                final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token, defaultApnsTopic, payload);
                final Future<PushNotificationResponse<SimpleApnsPushNotification>> notificationSendFuture = apnsClient.sendNotification(pushNotification);

                notificationSendFuture.addListener(future -> {

                    if (future.isSuccess()) {
                        handlePushNotificationResponsePerToken(notificationSendFuture.get(), pushMessageInformationId, variant.getId());
                    }
                });
            });

        } else {
            logger.error("Unable to send notifications, client is not connected. Removing from cache pool");
            senderCallback.onError("Unable to send notifications, client is not connected");
            variantUpdateEventEvent.fire(new iOSVariantUpdateEvent(iOSVariant));
        }
    }

    private void handlePushNotificationResponsePerToken(final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse, final String pushMessageInformationId, final String variantID) {

        final String deviceToken = pushNotificationResponse.getPushNotification().getToken();

        if (pushNotificationResponse.isAccepted()) {

            // Sends success to the "agpush_apnsTokenDeliveryMetrics" topic
            tokenDeliveryMetricsProducer.send(KAFKA_APNS_TOKEN_DELIVERY_METRICS_TOPIC, pushMessageInformationId, KAFKA_APNS_TOKEN_DELIVERY_SUCCESS);

            logger.trace("Push notification for '{}' (payload={})", deviceToken, pushNotificationResponse.getPushNotification().getPayload());

        } else {

            final String rejectReason = pushNotificationResponse.getRejectionReason();
            logger.trace("Push Message has been rejected with reason: {}", rejectReason);

            // Sends failure to the "agpush_apnsTokenDeliveryMetrics" topic
            tokenDeliveryMetricsProducer.send(KAFKA_APNS_TOKEN_DELIVERY_METRICS_TOPIC, pushMessageInformationId, KAFKA_APNS_TOKEN_DELIVERY_FAILURE);

            // token is either invalid, or did just expire
            if ((pushNotificationResponse.getTokenInvalidationTimestamp() != null) || ("BadDeviceToken".equals(rejectReason))) {
                logger.info(rejectReason + ", removing token: " + deviceToken);

                // add invalid token to a Kafka Topic
                invalidTokenProducer.send(KAFKA_INVALID_TOKEN_TOPIC, variantID, deviceToken);
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
                .setSoundFileName(message.getSound())
                .setAlertTitle(apns.getTitle())
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
        return simpleApnsClientCache.getApnsClientForVariant(iOSVariant, () -> {
            final ApnsClient apnsClient = buildApnsClient(iOSVariant);

            // connect and wait:
            logger.error("establishing the connection for {}", iOSVariant.getVariantID());
            connectToDestinations(iOSVariant, apnsClient);

            // APNS client has auto-reconnect, but let's log when that happens
            apnsClient.getReconnectionFuture().addListener(future -> logger.trace("Reconnecting to APNs"));
            return apnsClient;
        });
    }

    private ApnsClient buildApnsClient(final iOSVariant iOSVariant) {

        // this check should not be needed, but you never know:
        if (iOSVariant.getCertificate() != null && iOSVariant.getPassphrase() != null) {

            // add the certificate:
            try {
                final ByteArrayInputStream stream = new ByteArrayInputStream(iOSVariant.getCertificate());

                final ApnsClientBuilder builder = new ApnsClientBuilder();
                builder.setClientCredentials(stream, iOSVariant.getPassphrase());

                final ApnsClient apnsClient = builder.build();

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
