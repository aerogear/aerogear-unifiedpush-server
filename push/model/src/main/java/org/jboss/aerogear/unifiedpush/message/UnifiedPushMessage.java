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
 *           "localized-key" : "some value",
 *           "localized-arguments" : ["args1","arg2"],
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
     * Returns a JSON representation of the payload. This does not include any pushed data,
     * just the alert of the message. This also contains the entire criteria object.
     *
     * @see #toMinimizedJsonString()
     *
     * @return JSON payload
     */
    public String toStrippedJsonString() {
        try {
            final HashMap<String, Object> json = new LinkedHashMap<String, Object>();
            json.put("alert", this.message.getAlert());
            if (this.getMessage().getBadge()>0) {
                json.put("badge", Integer.toString(this.getMessage().getBadge()));
            }
            json.put("criteria", this.criteria);
            json.put("config", this.config);
            return OBJECT_MAPPER.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            return "[\"invalid json\"]";
        } catch (IOException e) {
            return "[\"invalid json\"]";
        }
    }

    /**
     * Returns a minimized JSON representation of the payload. This does not include potentially large objects, like
     * alias or category from the given criteria.
     *
     * @see #toStrippedJsonString()
     *
     * @return minizmized JSON payload
     */
    public String toMinimizedJsonString() {
        try {
            final HashMap<String, Object> json = new LinkedHashMap<String, Object>();
            json.put("alert", this.message.getAlert());
            if (this.getMessage().getBadge()>0) {
                json.put("badge", Integer.toString(this.getMessage().getBadge()));
            }
            json.put("config", this.config);

            // we strip down the criteria too, as alias/category can be quite long, based on use-case
            final HashMap<String, Object> shrinkedCriteriaJSON = new LinkedHashMap<String, Object>();
            shrinkedCriteriaJSON.put("variants", this.criteria.getVariants());
            shrinkedCriteriaJSON.put("deviceType", this.criteria.getDeviceTypes());
            json.put("criteria", shrinkedCriteriaJSON);

            return OBJECT_MAPPER.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            return "[\"invalid json\"]";
        } catch (IOException e) {
            return "[\"invalid json\"]";
        }
    }

    // used in java-sender
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
