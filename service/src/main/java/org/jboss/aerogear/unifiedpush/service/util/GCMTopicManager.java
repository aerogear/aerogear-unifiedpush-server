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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Category;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

/**
 *  Utility used for Android installations which need to be unsubscribed from topics
 */
public class GCMTopicManager {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(GCMTopicManager.class);

    // Instance ID API URL
    public static final String IIDURL = "https://iid.googleapis.com/iid/v1/";

    private AndroidVariant variant;

    public GCMTopicManager(AndroidVariant variant) {
        this.variant = variant;
    }

    /**
     * Unsubscribes device from single category(topic)
     *
     * @param installation Installation object containing correct variant property of AndroidVariant class
     * @param categoryToUnsubscribe category(topic) that device should be unsubscribed from
     */
    public void unsubscribe(Installation installation, Category categoryToUnsubscribe) {

        String url = "";
        try {
            url = IIDURL + installation.getDeviceToken() + "/rel/topics/" + URLEncoder.encode(categoryToUnsubscribe.getName(), "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            //
        }

        try {
            delete(url);
        } catch (IOException e) {
            logger.fine("Unregistering device from topic was unsuccessfull");
        }
    }

    /**
     * Sends DELETE HTTP request to provided URL. Request is authorized using Google API key.
     *
     * @param urlS target URL string
     */
    protected int delete(String urlS) throws IOException {
        URL url = new URL(urlS);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded" );
        conn.setRequestProperty("Authorization", "key=" + variant.getGoogleKey());
        conn.setRequestMethod("DELETE");
        conn.connect();
        return conn.getResponseCode();
    }

}