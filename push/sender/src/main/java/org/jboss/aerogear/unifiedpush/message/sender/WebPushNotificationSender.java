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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@SenderType(VariantType.WEB_PUSH)
public class WebPushNotificationSender implements PushNotificationSender {
    
    private final Logger logger = LoggerFactory.getLogger(WebPushNotificationSender.class);
    
    private static final String MPS_URL = "https://updates.push.services.mozilla.com/wpush/v1/";

    @Override
    public void sendPushMessage(Variant variant, Collection<String> clientIdentifiers, UnifiedPushMessage pushMessage,
            String pushMessageInformationId, NotificationSenderCallback senderCallback) {
        
        final int ttl = pushMessage.getConfig().getTimeToLive();

        int successCount = 0;
        for (String endpoint : clientIdentifiers) {
            try {
                HttpResponse response = Request.Post(MPS_URL + endpoint)
                        .addHeader("TTL", String.valueOf(ttl))
                        .execute()
                        .returnResponse();
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == HttpStatus.SC_CREATED) {
                    senderCallback.onSuccess();
                    successCount++;
                } else {
                    String content = EntityUtils.toString(response.getEntity());
                    logger.error("Error sending payload to MPS. Response status {}, content: {}", statusCode, content);
                    senderCallback.onError(content);
                }
            } catch (Exception e) {
                logger.error("Error sending push message to MPS", e);
                senderCallback.onError(e.getMessage());
            }
        }
        logger.info("Sent push notification to MPS Server for {} tokens of {}", successCount, clientIdentifiers.size());
    }
}
