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
package org.jboss.aerogear.unifiedpush.service.util;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

/**
 *  Utility used for Android installations which need to be unsubscribed from topics
 */
public class FCMTopicManager {

    private static final Logger logger = LoggerFactory.getLogger(FCMTopicManager.class);

    // Instance ID API URL
    public static final String IID_URL = "https://iid.googleapis.com/iid/v1/";

    private AndroidVariant variant;

    public FCMTopicManager(AndroidVariant variant) {
        this.variant = variant;
    }

    public Set<String> getSubscribedCategories(Installation installation) {
        String url = IID_URL + "info/" + installation.getDeviceToken() + "?details=true";
        String deviceInfo;
        try {
            deviceInfo = get(url);
        } catch (IOException e) {
            logger.debug("Couldn't get list of subscribed topics from Instance ID service.");
            return Collections.emptySet();
        }
        JSONParser parser = new JSONParser();
        JSONObject info;
        try {
            info = (JSONObject) parser.parse(deviceInfo);
        } catch (ParseException e) {
            logger.debug("Couldn't parse list of subscribed topics from Instance ID service.");
            return Collections.emptySet();
        }
        JSONObject rel = (JSONObject) info.get("rel");
        if (rel == null){
            logger.debug("Couldn't parse rel object Instance ID service.");
            return Collections.emptySet();
        }
        JSONObject topics = (JSONObject) rel.get("topics");
        return topics.keySet();
    }


    /**
     * Unsubscribes device from single category(topic)
     *
     * @param installation Installation object containing correct variant property of AndroidVariant class
     * @param categoryToUnsubscribe category(topic) that device should be unsubscribed from
     */
    public void unsubscribe(Installation installation, String categoryToUnsubscribe) {

        String url = "";
        try {
            url = IID_URL + installation.getDeviceToken() + "/rel/topics/" + URLEncoder.encode(categoryToUnsubscribe, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e1) {
            //
        }

        try {
            delete(url);
        } catch (IOException e) {
            logger.debug("Unregistering device from topic was unsuccessfull");
        }
    }

    /**
     * Sends DELETE HTTP request to provided URL. Request is authorized using Google API key.
     *
     * @param urlS target URL string
     */
    private int delete(String urlS) throws IOException {
        URL url = new URL(urlS);
        HttpURLConnection conn = prepareAuthorizedConnection(url);
        conn.setRequestMethod("DELETE");
        conn.connect();
        return conn.getResponseCode();
    }

    /**
     * Sends GET HTTP request to provided URL. Request is authorized using Google API key.
     *
     * @param urlS target URL string
     */
    private String get(String urlS) throws IOException {
        URL url = new URL(urlS);
        HttpURLConnection conn = prepareAuthorizedConnection(url);
        conn.setRequestMethod("GET");
        // Read response
        StringBuilder result = new StringBuilder();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }

    private HttpURLConnection prepareAuthorizedConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded" );
        conn.setRequestProperty("Authorization", "key=" + variant.getGoogleKey());
        return conn;
    }

}
