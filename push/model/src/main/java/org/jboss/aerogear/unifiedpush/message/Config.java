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
package org.jboss.aerogear.unifiedpush.message;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Configuration options of the UnifiedPush Message.
 *
 * <p>
 * For details have a look at the <a href="http://aerogear.org/docs/specs/aerogear-push-messages/">Message Format Specification</a>.
 */
public class Config implements Serializable {
    @JsonProperty("ttl")
    private int timeToLive = -1;

    /**
     * Returns the value of the 'ttl' key from the submitted payload.
     * This key is recognized for the Android and iOS Push Notification Service.
     *
     * If the 'ttl' key has not been specified on the submitted payload, this method will return -1.
     */
    public int getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(int timeToLive) {
        this.timeToLive = timeToLive;
    }

    @Override
    public String toString() {
        return "Config{" +
                "timeToLive=" + timeToLive +
                '}';
    }
}
