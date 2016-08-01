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

import nl.martijndwars.webpush.GcmNotification;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.jboss.aerogear.unifiedpush.dto.Token;
import org.jboss.aerogear.unifiedpush.dto.WebPushToken;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Collection;

@SenderType(VariantType.WEB_PUSH)
public class WebPushNotificationSender implements PushNotificationSender {

    private final Logger logger = LoggerFactory.getLogger(WebPushNotificationSender.class);

    private enum WebPushProvider {

        MPS("https://updates.push.services.mozilla.com/wpush/v1/"),
        FCM("https://fcm.googleapis.com/fcm/send");

        private final String url;

        WebPushProvider(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public static WebPushProvider defineProvider(String endpoint) {
            if (endpoint.startsWith(MPS.getUrl())) {
                return MPS;
            } else {
                return FCM;
            }
        }
    }

    @Override
    public void sendPushMessage(Variant variant, Collection<Token> tokens, UnifiedPushMessage pushMessage,
            String pushMessageInformationId, NotificationSenderCallback senderCallback) {

        final PushService pushService = new PushService(((WebPushVariant) variant).getFcmServerKey());

        final byte[] payload = pushMessage.getMessage().getAlert().getBytes();
        final int ttl = pushMessage.getConfig().getTimeToLive() > 0 ? pushMessage.getConfig().getTimeToLive() : 0;

        for (Token token : tokens) {
            try {
                final WebPushToken wpToken = (WebPushToken) token;
                final PublicKey publicKey = loadP256Dh(wpToken.getPublicKey());
                final byte[] authSecret = Base64.getUrlDecoder().decode(wpToken.getAuthSercret());
                final Notification notification;

                final WebPushProvider provider = WebPushProvider.defineProvider(token.getEndpoint());
                switch (provider) {
                    case MPS:
                        notification = new Notification(
                                wpToken.getEndpoint(),
                                publicKey,
                                authSecret,
                                payload,
                                ttl
                        );
                        break;
                    case FCM:
                        notification = new GcmNotification(
                                wpToken.getEndpoint(),
                                publicKey,
                                authSecret,
                                payload
                        );
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported WebPush provider: " + provider);
                }

                pushService.send(notification);
            } catch (Exception e) {
                logger.error("Error sending push message", e);
                senderCallback.onError(e.getMessage());
            }
        }
        logger.info("Sent {} web push notification(s)", tokens.size());
    }

    public PublicKey loadP256Dh(final String p256dh)
            throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {

        final byte[] p256dhBytes = Base64.getUrlDecoder().decode(p256dh);

        KeyFactory _keyFactory = KeyFactory.getInstance("ECDH", "BC");
        ECNamedCurveParameterSpec _ecNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("prime256v1"); // P256 curve
        ECNamedCurveSpec _ecNamedCurveSpec = new ECNamedCurveSpec("prime256v1", _ecNamedCurveParameterSpec.getCurve(), _ecNamedCurveParameterSpec
                .getG(), _ecNamedCurveParameterSpec.getN());

        final ECPoint point = ECPointUtil.decodePoint(_ecNamedCurveSpec.getCurve(), p256dhBytes);
        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, _ecNamedCurveSpec);
        return _keyFactory.generatePublic(pubKeySpec);
    }
}
