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

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.util.EntityUtils;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.jboss.aerogear.unifiedpush.dto.Token;
import org.jboss.aerogear.unifiedpush.dto.WebPushToken;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.util.webpush.encryption.WebPushEncryptedData;
import org.jboss.aerogear.unifiedpush.message.util.webpush.encryption.WebPushEncryptionUtil;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.Base64.Encoder;
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
    public void sendPushMessage(Variant variant, Collection<Token> clientIdentifiers, UnifiedPushMessage pushMessage,
            String pushMessageInformationId, NotificationSenderCallback senderCallback) {

        int ttl = pushMessage.getConfig().getTimeToLive();
        if (ttl == -1) {
            ttl = 0;
        }

        int successCount = 0;
        for (Token token : clientIdentifiers) {
            try {
                WebPushToken wpToken = (WebPushToken) token;
                WebPushEncryptedData data = WebPushEncryptionUtil
                        .generateEncryptedPayload(wpToken, pushMessage.getMessage().getAlert());
                logger.info("WebPushEncryptionUtil result: {}", data);

                final WebPushProvider wpp = WebPushProvider.defineProvider(wpToken.getEndpoint());

                final String postUrl = getPostUrl(wpToken.getEndpoint(), wpp);

                final Request request = Request
                        .Post(postUrl)
                        .addHeader("TTL", String.valueOf(ttl));

                switch (wpp) {
                    case MPS:
                        Encoder base64urlEncoder = Base64.getUrlEncoder().withoutPadding();
                        request.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM.toString())
                                .addHeader(HttpHeaders.CONTENT_ENCODING, "aesgcm128")
                                .addHeader("Encryption-Key", "keyid=p256dh;dh=" + base64urlEncoder.encode(data.getDh()))
                                .addHeader("Encryption", "keyid=p256dh;salt=" + base64urlEncoder.encode(data.getSalt()))
                                .bodyByteArray(data.getCiphertext());
                        break;
                    case FCM:
                        final JSONObject jsonObject = new JSONObject();
                        jsonObject.put("to", extractSubscriptionId(wpToken.getEndpoint()));
                        jsonObject.put("raw_data", Base64.getEncoder().encode(data.getCiphertext()));
                        final String body = jsonObject.toJSONString();

                        WebPushVariant wpVariant = (WebPushVariant) variant;
                        request.addHeader(HttpHeaders.AUTHORIZATION, "key=" + wpVariant.getFcmServerKey())
                                .addHeader(HttpHeaders.CONTENT_ENCODING, "aesgcm")
                                .addHeader("Crypto-Key", "dh=" + Base64.getUrlEncoder().encode(data.getDh()))
                                .addHeader("Encryption", "keyid=p256dh;salt=" + Base64.getUrlEncoder().encode(data.getSalt()))
                                .bodyString(body, ContentType.APPLICATION_JSON);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported WebPush provider: " + wpp);
                }

                logger.debug("WebPush request to {}: {}", wpp, request);

                final HttpResponse response = request
                        .execute()
                        .returnResponse();

                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode >= HttpStatus.SC_OK && statusCode <= HttpStatus.SC_NO_CONTENT) {
                    senderCallback.onSuccess();
                    successCount++;
                } else {
                    String content = EntityUtils.toString(response.getEntity());
                    logger.error("Error sending payload to {}. Response status {}, content: {}",
                            wpp, statusCode, content);
                    senderCallback.onError(content);
                }
            } catch (Exception e) {
                logger.error("Error sending push message", e);
                senderCallback.onError(e.getMessage());
            }
        }
        logger.info("Sent {} web push notification of {}", successCount, clientIdentifiers.size());
    }

    private static String getPostUrl(String endpoint, WebPushProvider wpp) {
        final String postUrl;
        switch (wpp) {
            case MPS:
                postUrl = endpoint;
                break;
            case FCM:
                postUrl = wpp.getUrl();
                break;
            default:
                throw new IllegalArgumentException("Unsupported WebPush provider: " + wpp);
        }
        return postUrl;
    }

    private static String extractSubscriptionId(String endpoint) {
        final int idx = endpoint.lastIndexOf('/');
        if (idx < 0) {
            throw new IllegalArgumentException("Can not extract subscriptionId from the endpoint: " + endpoint);
        }
        return endpoint.substring(idx + 1);
    }
}
