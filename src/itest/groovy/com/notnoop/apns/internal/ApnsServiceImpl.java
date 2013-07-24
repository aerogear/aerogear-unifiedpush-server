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
package com.notnoop.apns.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.notnoop.apns.ApnsService;

public class ApnsServiceImpl implements ApnsService {

    public ApnsServiceImpl() {

    }

    public static Collection<String> tokensList = null;

    public static String alert = null;

    public static String sound = null;

    public static int badge = -1;

    public void start() {
    }

    public void stop() {
    }

    public void testConnection() {

    }

    public Map<String, Date> getInactiveDevices() {
        return new HashMap<String, Date>();
    }

    @SuppressWarnings("rawtypes")
    public Collection push(Collection<String> tokens, String message) {
        if (tokens != null) {
            tokensList = new ArrayList<String>();
            tokensList.addAll(tokens);
        }

        if (message != null) {
            String[] parts = message.split(",");
            for (String part : parts) {
                String[] subparts = part.split(":");
                if ("alert".equals(subparts[0]))
                    alert = subparts[1];
                else if ("sound".equals(subparts[0]))
                    sound = subparts[1];
                else
                    badge = subparts[1] != null ? Integer.parseInt(subparts[1]) : -1;
            }
        }
        return null;
    }

    public static void clear() {
        tokensList = null;
        alert = null;
        sound = null;
        badge = -1;
    }
}