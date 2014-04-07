package org.jboss.aerogear.unifiedpush.message.sender;
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

import org.jboss.aerogear.unifiedpush.api.ChromePackagedAppVariant;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.helper.ChromePackagedAppTokenCache;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GCMForChromePushNotificationSender implements Serializable {

    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String MESSAGE_URL = "https://www.googleapis.com/gcm_for_chrome/v1/messages";
    private static final String ACCESS_TOKEN_URL = "https://accounts.google.com/o/oauth2/token";

    private final Logger logger = Logger.getLogger(GCMForChromePushNotificationSender.class.getName());

    // We need a place to hold the current access token/expire time for each GCM for Chrome application. Not good practice to always get a new access token
    private Map<String, ChromePackagedAppTokenCache> accessTokenMap = new HashMap<String, ChromePackagedAppTokenCache>();

    public void sendMessage( ChromePackagedAppVariant chromePackagedAppVariant, List<String> channelIDs, UnifiedPushMessage unifiedPushMessage) {
        // no need to send empty list
        if(channelIDs.isEmpty()) {
            return;
        }

        String accessToken = fetchAccessToken(chromePackagedAppVariant);

        // iterate over all the given channelIDs
        for (String channelID : channelIDs) {

            HttpURLConnection conn = null;
            try {
                final String clientURL = MESSAGE_URL;
                // POST the payload to the GCM For Chrome server
                conn = post(clientURL, "{'channelId': '" + channelID + "', 'subchannelId': '0', 'payload': '" + unifiedPushMessage.getAlert() + "'}", accessToken);
                int chromePackagedAppStatusCode = conn.getResponseCode();
                logger.info("GCM for Chrome Status: " + chromePackagedAppStatusCode);

                if (chromePackagedAppStatusCode >= 400) {
                    logger.fatal("ERROR ??????     STATUS CODE, from GCM for Chrome was NOT 2XX, but....: " + chromePackagedAppStatusCode);
                }
            } catch (IOException e) {
                logger.log(Level.FATAL, "Error during Post execution to GCM for Chrome Network", e);
            } finally {
                // tear down
                if (conn != null ) {
                    conn.disconnect();
                }
            }
        }
    }

    /**
     * Returns HttpURLConnection that 'posts' the given body to the given URL.
     */
    protected HttpURLConnection post(String url, String body, String accessToken) throws IOException {

        if (url == null || body == null) {
            throw new IllegalArgumentException("arguments cannot be null");
        }

        byte[] bytes = body.getBytes(UTF_8);
        HttpURLConnection conn = getConnection(url);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken );
        conn.setRequestMethod("POST");
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

    protected HttpURLConnection refreshAccessToken( ChromePackagedAppVariant chromePackagedAppVariant ) throws IOException{

        String body = "client_secret="+chromePackagedAppVariant.getClientSecret()+"&grant_type=refresh_token&refresh_token="+chromePackagedAppVariant.getRefreshToken()+"&client_id="+chromePackagedAppVariant.getClientId();

        byte[] bytes = body.getBytes(UTF_8);
        HttpURLConnection conn = getConnection(ACCESS_TOKEN_URL);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
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
     *
     * @param chromePackagedAppVariant
     * @return a valid access token
     */

    protected String fetchAccessToken(ChromePackagedAppVariant chromePackagedAppVariant) {
        HttpURLConnection accessTokenConn = null;
        JSONParser jsonParser = new JSONParser();
        ChromePackagedAppTokenCache accessTokenObject = null;
        String accessToken = null;
        long expireTime = 0; // in milliseconds

        try {
            // Not good practice to always get a new access token. so only get one if it is expired or null
            accessTokenObject = accessTokenMap.get(chromePackagedAppVariant.getClientId());

            if( accessTokenObject != null ) {
                accessToken = accessTokenObject.getAccessToken();
                expireTime = accessTokenObject.getExpiresIn();
            }

            if( accessToken == null || expireTime < new Date().getTime() ) {
                accessTokenConn = refreshAccessToken(chromePackagedAppVariant);
                String stringResponse = getString(accessTokenConn.getInputStream());
                accessToken = ((JSONObject)jsonParser.parse(stringResponse)).get("access_token").toString();
                String expiresIn = ((JSONObject)jsonParser.parse(stringResponse)).get("expires_in").toString();

                // Convert to millis
                long ex = Long.parseLong( expiresIn );
                expireTime = new Date().getTime() + (ex * 1000);

                if( accessTokenObject == null ) {
                    accessTokenObject = new ChromePackagedAppTokenCache();
                }

                accessTokenObject.setAccessToken(accessToken);
                accessTokenObject.setExpiresIn(expireTime);

                accessTokenMap.put(chromePackagedAppVariant.getClientId(),accessTokenObject);
            }
        } catch (IOException e) {
            logger.log(Level.FATAL, "Error during Post execution to GCM for Chrome Network For access token refresh", e);
        } catch (ParseException e) {
            logger.log(Level.FATAL, "Error during Parse of Response ", e);
        } finally {
            // tear down
            if (accessTokenConn != null ) {
                accessTokenConn.disconnect();
            }
        }

        return accessToken;
    }

    /**
     * Convenience method to open/establish a HttpURLConnection.
     */
    protected HttpURLConnection getConnection(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        return conn;
    }

    /**
     * Convenience method to convert an InputStream to a String.
     *
     * <p>
     * If the stream ends in a newline character, it will be stripped.
     */
    protected static String getString(InputStream stream) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(nonNull(stream)));
        StringBuilder content = new StringBuilder();
        String newLine;
        do {
            newLine = reader.readLine();
            if (newLine != null) {
                content.append(newLine).append('\n');
            }
        } while (newLine != null);
        if (content.length() > 0) {
            // strip last newline
            content.setLength(content.length() - 1);
        }
        return content.toString();
    }

    static <T> T nonNull(T argument) {
        if (argument == null) {
            throw new IllegalArgumentException("argument cannot be null");
        }
        return argument;
    }
}
