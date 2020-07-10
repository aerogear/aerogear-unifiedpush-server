/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.sender.apns;

import com.eatthepath.pushy.apns.*;
import com.eatthepath.pushy.apns.auth.ApnsSigningKey;
import com.eatthepath.pushy.apns.proxy.HttpProxyHandlerFactory;
import com.eatthepath.pushy.apns.proxy.Socks5ProxyHandlerFactory;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.iOSTokenVariant;
import org.jboss.aerogear.unifiedpush.event.APNSVariantUpdateEvent;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.Message;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.apns.APNs;
import org.jboss.aerogear.unifiedpush.message.cache.SimpleApnsClientCache;
import org.jboss.aerogear.unifiedpush.message.sender.NotificationSenderCallback;
import org.jboss.aerogear.unifiedpush.message.sender.PushNotificationSender;
import org.jboss.aerogear.unifiedpush.message.sender.SenderType;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.metrics.PrometheusExporter;
import org.jboss.aerogear.unifiedpush.service.proxy.ProxyConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.net.ssl.SSLException;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.jboss.aerogear.unifiedpush.system.ConfigurationUtils.tryGetGlobalIntegerProperty;
import static org.jboss.aerogear.unifiedpush.system.ConfigurationUtils.tryGetGlobalProperty;

@Stateless
@SenderType(VariantType. IOS_TOKEN)
public class PushyIosTokenSender implements PushNotificationSender {

    private static final Logger logger = LoggerFactory.getLogger(PushyIosTokenSender.class);

    public static final String CUSTOM_AEROGEAR_APNS_PUSH_HOST = "custom.aerogear.apns.push.host";
    public static final String CUSTOM_AEROGEAR_APNS_PUSH_PORT = "custom.aerogear.apns.push.port";
    private static final String customAerogearApnsPushHost = tryGetGlobalProperty(CUSTOM_AEROGEAR_APNS_PUSH_HOST);
    private static final Integer customAerogearApnsPushPort = tryGetGlobalIntegerProperty(CUSTOM_AEROGEAR_APNS_PUSH_PORT);

    private final ConcurrentSkipListSet<String> invalidTokens = new ConcurrentSkipListSet();

    @Inject
    private SimpleApnsClientCache simpleApnsClientCache;
    @Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private Event<APNSVariantUpdateEvent> variantUpdateEventEvent;

    @Override
    public void sendPushMessage(final Variant variant, final Collection<String> tokens, final UnifiedPushMessage pushMessage,
                                final String pushMessageInformationId, final NotificationSenderCallback senderCallback) {
        // no need to send empty list
        if (tokens.isEmpty()) {
            return;
        }

        final iOSTokenVariant apnsVariant = (iOSTokenVariant) variant;

        handleTokenVariant(apnsVariant, senderCallback, pushMessage, pushMessageInformationId, tokens);


    }

    private void handleTokenVariant(iOSTokenVariant iOSTokenVariant, NotificationSenderCallback senderCallback, UnifiedPushMessage pushMessage, String pushMessageInformationId, Collection<String> tokens) {
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
                apnsClient = receiveApnsConnection(iOSTokenVariant);
            } catch (IllegalArgumentException iae) {
                logger.error(iae.getMessage(), iae);
                senderCallback.onError(String.format("Unable to connect to APNs (%s))", iae.getMessage()));
                return;
            }
        }

        if (apnsClient != null) {

            // we are connected and are about to send
            // notifications to all tokens of the batch
            PrometheusExporter.instance().increaseTotalPushIosRequests();

            // we have managed to connect and will send tokens ;-)
            senderCallback.onSuccess();

            final String defaultApnsTopic = iOSTokenVariant.getBundleId();
            Date expireDate = createFutureDateBasedOnTTL(pushMessage.getConfig().getTimeToLive());
            logger.debug("sending payload for all tokens for {} to APNs ({})", iOSTokenVariant.getVariantID(), defaultApnsTopic);

            tokens.forEach(token -> {
                final SimpleApnsPushNotification pushNotification = new SimpleApnsPushNotification(token,
                        defaultApnsTopic, payload, expireDate.toInstant(), DeliveryPriority.IMMEDIATE,
                        determinePushType(pushMessage.getMessage()), null, null);
                final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> notificationSendFuture = apnsClient
                        .sendNotification(pushNotification);

                notificationSendFuture.whenComplete((result, cause) -> {

                    if (result != null) {
                        handlePushNotificationResponsePerToken(result);
                    } else {
                        logger.error("Could not send iOS message", cause);
                    }
                });
            });

        } else {
            logger.error("Unable to send notifications, client is not connected. Removing from cache pool");
            senderCallback.onError("Unable to send notifications, client is not connected");
            variantUpdateEventEvent.fire(new APNSVariantUpdateEvent(iOSTokenVariant));
        }
    }

    /**
     * Helper method that creates a future {@link Date}, based on the given ttl/time-to-live value.
     * If no TTL was provided, we use the default value from the APNs library
     */
    private Date createFutureDateBasedOnTTL(int ttl) {
        // no TTL was specified on the payload, we use the Default from the APNs library:
        if (ttl < 0) {
            return new Date(System.currentTimeMillis() + SimpleApnsPushNotification.DEFAULT_EXPIRATION_PERIOD.toMillis());
        } else {
            // apply the given TTL to the current time
            return new Date(System.currentTimeMillis() + ttl * 1000L);
        }
    }

    private PushType determinePushType(Message message) {
        if (!isEmpty(message.getAlert()) || !isEmpty(message.getSound())) {
            return PushType.ALERT;
        }
        return PushType.BACKGROUND;
    }

    private boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private void handlePushNotificationResponsePerToken(
            final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse) {

        final String deviceToken = pushNotificationResponse.getPushNotification().getToken();

        if (pushNotificationResponse.isAccepted()) {
            logger.trace("Push notification for '{}' (payload={})", deviceToken,
                    pushNotificationResponse.getPushNotification().getPayload());
        } else {
            final String rejectReason = pushNotificationResponse.getRejectionReason();
            logger.trace("Push Message has been rejected with reason: {}", rejectReason);

            // token is either invalid, or did just expire
            if ((pushNotificationResponse.getTokenInvalidationTimestamp() != null) || ("BadDeviceToken".equals(rejectReason))) {
                logger.info(rejectReason + ", removing token: " + deviceToken);

                invalidTokens.add(deviceToken);
            }
        }
    }

    private String createPushPayload(final Message message, final String pushMessageInformationId) {
        final ApnsPayloadBuilder payloadBuilder = new SimpleApnsPayloadBuilder();
        final APNs apns = message.getApns();

        // only set badge if needed/included in user's payload
        if (message.getBadge() >= 0) {
            payloadBuilder.setBadgeNumber(message.getBadge());
        }

        payloadBuilder.addCustomProperty(InternalUnifiedPushMessage.PUSH_MESSAGE_ID, pushMessageInformationId)
                .setAlertBody(message.getAlert()).setSoundFileName(message.getSound()).setAlertTitle(apns.getTitle())
                .setActionButtonLabel(apns.getAction()).setUrlArguments(apns.getUrlArgs())
                .setCategoryName(apns.getActionCategory()).setContentAvailable(apns.isContentAvailable())
                .setMutableContent(apns.hasMutableContent());

        // custom fields
        final Map<String, Object> userData = message.getUserData();
        for (Map.Entry<String, Object> entry : userData.entrySet()) {
            payloadBuilder.addCustomProperty(entry.getKey(), entry.getValue());
        }

        return payloadBuilder.build();
    }

    private synchronized ApnsClient receiveApnsConnection(final iOSTokenVariant apnsVariant) {
        return simpleApnsClientCache.getApnsClientForVariant(apnsVariant, () -> {
            final ApnsClientBuilder builder = new ApnsClientBuilder();
            assambleApnsClientBuilderForToken( apnsVariant, builder);

            connectToDestinations(apnsVariant, builder);

            // connect and wait, ONLY when we have a valid client
            logger.debug("establishing the connection for {}", apnsVariant.getVariantID());
            ApnsClient apnsClient;
            try {
                logger.debug("connecting to APNs");
                apnsClient = builder.build();
                return apnsClient;
            } catch (SSLException e) {
                logger.error("Error connecting to APNs", e);
            }
            return null;

        });
    }

    private void assambleApnsClientBuilderForToken(final iOSTokenVariant iOSVariant, final ApnsClientBuilder builder) {

        // this check should not be needed, but you never know:
        if (iOSVariant.getTeamId() != null && iOSVariant.getKeyId() != null && iOSVariant.getPrivateKey() != null) {

            // add the token:
            try (final ByteArrayInputStream stream = new ByteArrayInputStream(iOSVariant.getPrivateKey().getBytes())) {

                builder.setSigningKey(
                        ApnsSigningKey.loadFromInputStream(stream,iOSVariant.getTeamId(), iOSVariant.getKeyId())
                );

                if (ProxyConfiguration.hasHttpProxyConfig()) {
                    if (ProxyConfiguration.hasBasicAuth()) {
                        String user = ProxyConfiguration.getProxyUser();
                        String pass = ProxyConfiguration.getProxyPass();
                        builder.setProxyHandlerFactory(
                                new HttpProxyHandlerFactory(ProxyConfiguration.proxyAddress(), user, pass));
                    } else {
                        builder.setProxyHandlerFactory(new HttpProxyHandlerFactory(ProxyConfiguration.proxyAddress()));
                    }

                } else if (ProxyConfiguration.hasSocksProxyConfig()) {
                    builder.setProxyHandlerFactory(new Socks5ProxyHandlerFactory(ProxyConfiguration.socks()));
                }
                return;
            } catch (Exception e) {
                logger.error("Error reading certificate", e);
            }
        }
        // indicating an incomplete service
        throw new IllegalArgumentException("Not able to construct APNS client");
    }

    private void connectToDestinations(final iOSTokenVariant iOSVariant, final ApnsClientBuilder builder) {

        String apnsHost;
        int apnsPort = ApnsClientBuilder.DEFAULT_APNS_PORT;

        // are we production or development ?
        if (iOSVariant.isProduction()) {
            apnsHost = ApnsClientBuilder.PRODUCTION_APNS_HOST;
        } else {
            apnsHost = ApnsClientBuilder.DEVELOPMENT_APNS_HOST;
        }

        // Or is there even a custom ost&port provided by a system property, for tests ?
        if (customAerogearApnsPushHost != null) {
            apnsHost = customAerogearApnsPushHost;

            if (customAerogearApnsPushPort != null) {
                apnsPort = customAerogearApnsPushPort;
            }
        }
        builder.setApnsServer(apnsHost, apnsPort);
    }
}
