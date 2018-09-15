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

import java.util.Collection;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.jboss.aerogear.unifiedpush.dto.Subscription;

@SenderType(VariantType.WEB_PUSH)
public class WebPushNotificationSender implements PushNotificationSender {

    private final Logger logger = LoggerFactory.getLogger(WebPushNotificationSender.class);

    /**
     * The Time to live of GCM notifications
     */
    private static final int TTL = 255;    
    
    private enum WebPushProvider {

        MPS("https://updates.push.services.mozilla.com/wpush/v1/"),
        FCM("https://fcm.googleapis.com/fcm/send"),
        CUSTOM("unknown");

        private final String url;

        WebPushProvider(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }
    }

    @Override
    public void sendPushMessage(Variant variant, Object clientIdentifiers, UnifiedPushMessage pushMessage,
            String pushMessageInformationId, NotificationSenderCallback senderCallback) {

        int ttl = pushMessage.getConfig().getTimeToLive();
        if (ttl == -1) {
            ttl = 0;
        }

        Collection<Subscription> subscriptions = (Collection<Subscription>) clientIdentifiers;
        int successCount = 0;
        for (Subscription subscription : subscriptions) {            
            try {
                final WebPushVariant wpVariant = (WebPushVariant) variant;
                final WebPushProvider wpp = defineProvider(subscription.getEndpoint());
                
                Notification notification = null;
                PushService pushService = null;                               

                switch (wpp) {
                    case MPS:
                        // Create a notification with the endpoint, userPublicKey from the subscription and a custom payload
                        notification = new Notification(
                                subscription.getEndpoint(),
                                subscription.getKey(),
                                subscription.getAuth(),
                                pushMessage.getMessage().getAlert()
                        );

                        // Instantiate the push service, no need to use an API key for Push API
                        pushService = new PushService();
                        break;
                    case FCM:

                        // Or create a GcmNotification, in case of Google Cloud Messaging
                        notification = new Notification(
                                subscription.getEndpoint(),
                                subscription.getUserPublicKey(),
                                subscription.getAuthAsBytes(),
                                pushMessage.getMessage().getAlert().getBytes("UTF-8"),
                                TTL
                        );

                        // Instantiate the push service with a GCM API key
                        pushService = new PushService(wpVariant.getFcmServerKey());
                        
                        break;
                    case CUSTOM:
                        // do nothing
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported WebPush provider: " + wpp);
                }

                HttpResponse response = pushService.send(notification);
                
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
        logger.info("Sent {} web push notification of {}", successCount, subscriptions.size());
    }

    private static WebPushProvider defineProvider(String endpoint) {
        if (endpoint.startsWith(WebPushProvider.MPS.getUrl())) {
            return WebPushProvider.MPS;
        } else {
            return WebPushProvider.FCM;
        }
    }

    private static String getPostUrl(String endpoint, WebPushProvider wpp) {
        final String postUrl;
        switch (wpp) {
            case MPS:
            case CUSTOM:
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

