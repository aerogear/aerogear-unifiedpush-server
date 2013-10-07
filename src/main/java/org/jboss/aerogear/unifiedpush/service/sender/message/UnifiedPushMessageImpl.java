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
package org.jboss.aerogear.unifiedpush.service.sender.message;

import java.util.Map;

/**
 * Contains the data of the JSON payload that has been sent to the
 * RESTful Sender endpoint.
 * 
 * <p>
 * For details have a look at the <a href="http://aerogear.org/docs/specs/aerogear-push-messages/">Message Format Specification</a>.
 */
public class UnifiedPushMessageImpl implements UnifiedPushMessage {

    private final SendCriterias criterias;

    private final Map<String, String> simplePush;
    private final String alert;
    private final String sound;
    private final int badge;

    private final Map<String, Object> data;

    /**
     * Messages are submitted as flexible JSON maps, like:
     * <pre>
     *   {
     *     "alias" : ["someUsername"],
     *     "deviceType" : ["someDevice"],
     *     "category" : "someCategory",
     *     "variants" : ["someVariantIDs"],
     *     "message":
     *     {
     *       "key":"value",
     *       "key2":"other value",
     *       "alert":"HELLO!",
     *       "sound":"default",
     *       "badge":2
     *     },
     *     "simple-push":
     *     {
     *       "SomeCategory":"version=123",
     *       "anotherCategory":"version=456"
     *     }
     *   }
     * </pre>
     * This class give some convenient methods to access the query components (<code>alias</code> or <code>deviceType</code>),
     * the <code>simple-push</code> value or some <i>highlighted</i> keywords.
     */
    @SuppressWarnings("unchecked")
    public UnifiedPushMessageImpl(Map<String, Object> data) {
        // extract all the different criterias
        this.criterias = new SendCriterias(data);

        // ======= Payload ====
        // the Android/iOS payload of the actual message:
        this.data = (Map<String, Object>) data.remove("message");
        // if 'native' message object is around, let's extract some data:
        if (this.data != null) {
            // remove the desired keywords:
            // special key words (for APNs)
            this.alert = (String) this.data.remove("alert"); // used in AGDROID as well
            this.sound = (String) this.data.remove("sound");

            Integer badgeVal = (Integer) this.data.remove("badge");
            if (badgeVal == null) {
                this.badge = -1;
            } else {
                this.badge = badgeVal;
            }
        } else {
            // satisfy the final
            this.alert = null;
            this.sound = null;
            this.badge = -1;
        }

        // SimplePush values: 
        this.simplePush = (Map<String, String>) data.remove("simple-push");

    }

    /**
     * Returns the object that contains all the submitted query criteria.
     */
    public SendCriterias getSendCriterias() {
        return criterias;
    }

    /**
     * Returns the SimplePush specific Map, containing the requested categories and their version strings:
     * <pre>
     *   {
     *     "SomeCategory":"version=123",
     *     "anotherCategory":"version=456"
     *   }
     * </pre>
     */
    public Map<String, String> getSimplePush() {
        return simplePush;
    }

    @Override
    public String getAlert() {
        return alert;
    }

    @Override
    public String getSound() {
        return sound;
    }

    @Override
    public int getBadge() {
        return badge;
    }

    @Override
    public Map<String, Object> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "UnifiedPushMessageImpl [criterias=" + criterias + ", simplePush=" + simplePush + ", alert=" + alert + ", sound=" + sound + ", badge=" + badge + ", data="
                + data + "]";
    }
}
