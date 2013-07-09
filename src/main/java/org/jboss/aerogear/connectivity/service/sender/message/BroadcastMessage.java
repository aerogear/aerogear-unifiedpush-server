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

package org.jboss.aerogear.connectivity.service.sender.message;

import java.util.Map;

public class BroadcastMessage implements UnifiedPushMessage {

    private final String simplePush;
    private final String alert;
    private final String sound;
    private final int badge;
    private final Map<String, Object> data;

    /**
     * Broadcast messages are submitted as flexible JSON maps, like:
     * <pre>
     *   {
     *     "key":"value",
     *     "alert":"HELLO!",
     *     "sound":"default",
     *     "badge":7,
     *     "simple-push":"version=123"
     *   }
     * </pre>
     * This class give some convenient methods to access some <i>highlighted</i> keywords.
     */
    public BroadcastMessage(Map<String, Object> data) {
        // simple push value
        this.simplePush = (String) data.remove("simple-push");

        // special key words (for APNs)
        this.alert = (String) data.remove("alert");  // used in AGDROID as well
        this.sound = (String) data.remove("sound");

        Integer badgeVal = (Integer) data.remove("badge");
        if (badgeVal == null) {
            this.badge = -1;
        } else {
            this.badge = badgeVal;
        }

        // rest of the data/map:
        this.data = data;
    }

    public String getSimplePush() {
        return simplePush;
    }

    public String getAlert() {
        return alert;
    }

    public String getSound() {
        return sound;
    }

    public int getBadge() {
        return badge;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
