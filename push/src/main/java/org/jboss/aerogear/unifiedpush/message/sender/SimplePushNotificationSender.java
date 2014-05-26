/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message.sender;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response.Status;

public class SimplePushNotificationSender implements PushNotificationSender {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private final Logger logger = Logger.getLogger(SimplePushNotificationSender.class.getName());

    /**
     * Sends SimplePush notifications to all connected clients, that are represented by
     * the {@link Collection} of channelIDs, for the given SimplePush network.
     */
    public void sendPushMessage(Variant variant, Collection<String> tokens, UnifiedPushMessage pushMessage, NotificationSenderCallback callback) {

        String payload = pushMessage.getSimplePush();

        // Convenience from the SimplePush spec;
        // supported by Moz and our SimplePush Server
        if (payload == null) {
            payload = System.currentTimeMillis()+"";
        }

        // iterate over all the given channels, if there are channels:
        for (String clientURL : tokens) {

            HttpURLConnection conn = null;
            try {
                // PUT the version payload to the SimplePushServer
                logger.log(Level.FINEST, "Sending out SimplePush payload: " + payload);
                conn = put(clientURL, payload);
                int simplePushStatusCode = conn.getResponseCode();
                logger.log(Level.INFO, "SimplePush Status: " + simplePushStatusCode);

                if (Status.OK.getStatusCode() != simplePushStatusCode) {
                    logger.log(Level.SEVERE, "Error during PUT execution to SimplePush Network, status code was: " + simplePushStatusCode);
                    callback.onError();
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error during PUT execution to SimplePush Network", e);
                callback.onError();
            } finally {
                callback.onSuccess();
                // tear down
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

    /**
     * Returns HttpURLConnection that 'puts' the given body to the given URL.
     */
    protected HttpURLConnection put(String url, String body) throws IOException {

        if (url == null || body == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }

        byte[] bytes = body.getBytes(UTF_8);
        HttpURLConnection conn = getConnection(url);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestProperty("Accept", "application/x-www-form-urlencoded");
        conn.setRequestMethod("PUT");
        OutputStream out = null;
        try {
            out = conn.getOutputStream();
            out.write(bytes);
        } finally {
            // in case something blows up, while writing
            // the payload, we wanna close the stream:
            if (out != null) {
                out.close();
            }
        }
        return conn;
    }

    /**
     * Convenience method to open/establish a HttpURLConnection.
     */
    protected HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        return conn;
    }
}
