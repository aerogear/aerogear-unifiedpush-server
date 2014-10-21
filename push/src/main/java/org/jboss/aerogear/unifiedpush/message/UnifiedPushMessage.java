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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contains the data of the JSON payload that has been sent to the
 * RESTful Sender endpoint.
 * 
 * <p>
 * For details have a look at the <a href="http://aerogear.org/docs/specs/aerogear-push-messages/">Message Format Specification</a>.
 *
 * Messages are submitted as flexible JSON maps, like:
 * <pre>
 *  "message": {
 *   "alert": "HELLO!",
 *   "action-category": "some value",
 *   "sound": "default",
 *   "badge": 2,
 *   "content-available": true,
 *   "payload": {
 *       "key": "value",
 *       "key2": "other value"
 *   },
 *   "simple-push": "version=123"
 *  },
 *  "criteria": {
 *      "alias": [ "someUsername" ],
 *      "deviceType": [ "someDevice" ],
 *      "categories": [ "someCategories" ],
 *      "variants": [ "someVariantIDs" ]
 *  },
 *  "config": {
 *      "ttl": 3600
 *  }
 * </pre>
 * This class give some convenient methods to access the query components (<code>alias</code> or <code>deviceType</code>),
 * the <code>simple-push</code> value or some <i>highlighted</i> keywords.
 */
public class UnifiedPushMessage {

    private String ipAddress;
    private String clientIdentifier;

    private Message message = new Message();

    private Criteria criteria = new Criteria();
    private Config config = new Config();

    /**
     * Returns the object that contains all the submitted query criteria.
     */
    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria) {
        this.criteria = criteria;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * The IP address from the agent that did issue the push message request.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * The Client Identifier showing who triggered the Push Notification
     */
    public String getClientIdentifier() { return clientIdentifier; }

    public void setClientIdentifier(String clientIdentifier) { this.clientIdentifier = clientIdentifier; }

    public String toJsonString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "[\"invalid json\"]";
        }
    }

    @Override
    public String toString() {
        return "[message=" + message + ", criteria="
                + criteria + ", time-to-live=" + config + "]";
    }
}
