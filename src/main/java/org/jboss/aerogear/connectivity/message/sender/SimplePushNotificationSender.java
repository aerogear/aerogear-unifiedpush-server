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
package org.jboss.aerogear.connectivity.message.sender;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.aerogear.connectivity.message.sender.annotations.SimplePushSender;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

@SimplePushSender
@ApplicationScoped
public class SimplePushNotificationSender implements Serializable {
    private static final long serialVersionUID = 5747687132270998712L;

    @Inject
    private Logger logger;

    public void sendMessage(String endpoint, String payload, List<String> channels) {
        // iterate over all the given channels:
        for (String channelID : channels) {

            HttpURLConnection conn = null;
            try {
                // PUT the version payload to the SimplePushServer 
                conn = put(endpoint+channelID, payload);
                int simplePushStatusCode = conn.getResponseCode();
                logger.info("SimplePush Status: " + simplePushStatusCode);

                if (200 != simplePushStatusCode) {
                    logger.severe("ERROR ??????     STATUS CODE, from PUSH NETWORK was NOT 200, but....: " + simplePushStatusCode);
                }
            } catch (IOException e) {
                e.printStackTrace();
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

        byte[] bytes = body.getBytes();
        HttpURLConnection conn = getConnection(url);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestProperty("Accept", "application/x-www-form-urlencoded");
        conn.setRequestMethod("PUT");
        OutputStream out = conn.getOutputStream();
        out.write(bytes);
        out.close();
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
