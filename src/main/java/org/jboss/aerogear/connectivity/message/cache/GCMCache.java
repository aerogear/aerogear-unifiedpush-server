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
package org.jboss.aerogear.connectivity.message.cache;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import com.google.android.gcm.server.Sender;

@ApplicationScoped
public class GCMCache implements Serializable {

    private static final long serialVersionUID = 8171485458336202582L;

    private final ConcurrentHashMap<String, Sender> cache = new ConcurrentHashMap<String, Sender>();

    public Sender getSenderForAPIKey(String googleAPIKey) {
        Sender sender = cache.get(googleAPIKey);

        if (sender == null) {
            // create and cache:
            sender = new Sender(googleAPIKey);
            cache.put(googleAPIKey, sender);
        }

        return sender;
    }
}
