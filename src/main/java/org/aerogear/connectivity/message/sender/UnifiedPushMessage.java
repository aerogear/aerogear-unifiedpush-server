/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.aerogear.connectivity.message.sender;

import java.util.Map;

// helper class
public class UnifiedPushMessage {
    private final String alert;
    private final String sound;
    private final int badge;
    private final Map<String, ? extends Object> data;

    public String getAlert() {
        return alert;
    }

    public String getSound() {
        return sound;
    }

    public int getBadge() {
        return badge;
    }

    public Map<String, ? extends Object> getData() {
        return data;
    }

    public UnifiedPushMessage(Map<String, ? extends Object> data) {
        // special key words (for APNs)
        this.alert = (String) data.remove("alert");
        this.sound = (String) data.remove("sound");
        
        Integer badgeVal = (Integer) data.remove("badge");
        if (badgeVal == null) {
            this.badge = -1;
        } else {
            this.badge = badgeVal;
        }

        // rest of the data:
        this.data = data;
    }
}