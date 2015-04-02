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


import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Contains the data of the JSON payload that has been sent to the
 * RESTful Sender endpoint.
 *
 * <p>
 * For details have a look at the <a href="http://aerogear.org/docs/specs/aerogear-push-messages/">Message Format Specification</a>.
 *
 * Messages are submitted as follows:
 * <pre>
 * {
 *   "message": {
 *       "alert": "HELLO!",
 *       "sound": "default",
 *       "badge": 2,
 *       "user-data": {
 *          "key": "value",
 *          "key2": "other value"
 *       },
 *       "windows": {
 *           "type": "tile",
 *           "duration": "short",
 *           "badge": "alert",
 *           "tileType": "TileWideBlockAndText01",
 *           "images": ["Assets/test.jpg", "Assets/background.png"],
 *           "textFields": ["foreground text"]
 *       },
 *       "apns": {
 *           "title" : "someTitle",
 *           "action-category": "some value",
 *           "content-available": true,
 *           "action" : "someAction",
 *           "url-args" :["args1","arg2"],
 *           "localized-title-key" : "some value",
 *           "localized-title-arguments" : ["args1","arg2"]
 *       },
 *       "simple-push": "version=123"
 *    },
 *    "criteria": {
 *         "alias": [ "someUsername" ],
 *         "deviceType": [ "someDevice" ],
 *         "categories": [ "someCategories" ],
 *         "variants": [ "someVariantIDs" ]
 *     },
 *    "config": {
 *         "ttl": 3600
 *     }
 * }
 * </pre>
 */
public class UnifiedPushMessage implements Serializable {

    private static final long serialVersionUID = -5978882928783277261L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    public String toStrippedJsonString() {
        try {
            final HashMap<String, Object> json = new LinkedHashMap<String, Object>();
            json.put("ipAddress", this.ipAddress);
            json.put("clientIdentifier", this.clientIdentifier);
            json.put("alert", this.message.getAlert());
            json.put("criteria", this.criteria);
            json.put("config", this.config);
            return OBJECT_MAPPER.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            return "[\"invalid json\"]";
        } catch (IOException e) {
            return "[\"invalid json\"]";
        }
    }

    public String toJsonString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "[\"invalid json\"]";
        } catch (IOException e) {
            return "[\"invalid json\"]";
        }
    }

    @Override
    public String toString() {
        return "[alert=" + message.getAlert() + ", criteria="
                + criteria + ", time-to-live=" + config + "]";
    }

}
