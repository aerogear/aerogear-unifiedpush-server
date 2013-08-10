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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

@ApplicationScoped
public class SimplePushNotificationSender implements Serializable {
    private static final long serialVersionUID = 5747687132270998712L;

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Inject
    private Logger logger;

    /**
     * Sends SimplePush notifications to all connected clients, that are represented by
     * the {@link Collection} of channelIDs, for the given SimplePush network.  
     * 
     * @param endpointBaseURL Base URL of the used SimplePush Network
     * @param payload the payload, or version string, to be submitted
     * @param channelIDs collection of channelIDs, representing actual SimplePush client (devices).
     */
    public void sendMessage(String endpointBaseURL, String payload, List<String> channelIDs) {
        // iterate over all the given channels, if there are channels:
        for (String channelID : channelIDs) {

            HttpURLConnection conn = null;
            try {
                final String clientURL = endpointBaseURL+channelID;
                // PUT the version payload to the SimplePushServer 
                logger.fine(String.format("Sending transformed SimplePush version: '%s' to %s", payload, clientURL));
                conn = put(clientURL, payload);
                int simplePushStatusCode = conn.getResponseCode();
                logger.info("SimplePush Status: " + simplePushStatusCode);

                if (200 != simplePushStatusCode) {
                    logger.severe("ERROR ??????     STATUS CODE, from PUSH NETWORK was NOT 200, but....: " + simplePushStatusCode);
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error during PUT execution to SimplePush Network", e);
            } finally {
                // tear down
                if (conn != null ) {
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
