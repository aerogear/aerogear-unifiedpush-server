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
package com.google.android.gcm.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.aerogear.connectivity.common.SenderSimulation;

@SenderSimulation
@ApplicationScoped
public class Sender {

    public static List<String> gcmRegIdsList = null;
    
    public static Message gcmMessage = null;

    private String key;

    public Sender(String key) {
        this.setKey(key);
    }

    public Sender() {
    }

    public Result send(Message message, String registrationId, int retries) throws IOException {
        return null;
    }

    public Result sendNoRetry(Message message, String registrationId) throws IOException {
        return null;
    }

    public MulticastResult send(Message message, List<String> regIds, int retries) throws IOException {
        if (regIds != null && !regIds.isEmpty()) {
            gcmRegIdsList = new ArrayList<String>();
            gcmRegIdsList.addAll(regIds);
        }
        if (message != null) {
            gcmMessage = message;
        }
        return null;
    }

    public MulticastResult sendNoRetry(Message message, List<String> registrationIds) throws IOException {
        return null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public static void clear() {
        gcmRegIdsList = null;
        gcmMessage = null;
    }
}
