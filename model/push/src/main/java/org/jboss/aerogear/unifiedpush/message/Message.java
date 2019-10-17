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


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.jboss.aerogear.unifiedpush.message.apns.APNs;
import org.jboss.aerogear.unifiedpush.message.json.PriorityDeserializer;
import org.jboss.aerogear.unifiedpush.message.json.PrioritySerializer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.aerogear.unifiedpush.message.Priority.NORMAL;

/**
 * The message part of the Unified message.
 * <p>
 * For details have a look at the <a href="https://aerogear.org/docs/unifiedpush/push-message-format/">AeroGear UnifiedPush Message Format</a>.
 */
public class Message implements Serializable {

    private static final long serialVersionUID = -4467505284880930113L;

    private String alert;
    private String sound;
    private int badge = -1;

    @JsonProperty("user-data")
    private Map<String, Object> userData = new HashMap<>();

    @JsonProperty("simple-push")
    private String simplePush;

    @JsonSerialize(using=PrioritySerializer.class)
    @JsonDeserialize(using=PriorityDeserializer.class)
    private Priority priority = NORMAL;

    private String consolidationKey;

    private APNs apns = new APNs();

    /**
     * Returns the value of the 'alert' key from the submitted payload.
     * This key is recognized in native iOS, without any API invocation and
     * on AeroGear's GCM offerings.
     *
     * Android users that are not using AGDROID can read the value as well,
     * but need to call specific APIs to show the 'alert' value.
     */
    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }


    /**
     * Returns the value of the 'sound' key from the submitted payload.
     * This key is recognized in native iOS, without any API invocation.
     *
     * Android users can read the value as well, but need to call specific
     * APIs to play the referenced 'sound' file.
     */
    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    /**
     * Returns the value of the 'badge' key from the submitted payload.
     * This key is recognized in native iOS, without any API invocation.
     *
     * Android users can read the value as well, but need to call specific
     * APIs to show the 'badge number'.
     */
    public int getBadge() {
        return badge;
    }

    public void setBadge(int badge) {
        this.badge = badge;
    }

    /**
     * Returns a Map, representing any other key-value pairs that were send
     * to the RESTful Sender API.
     *
     * This map usually contains application specific payload, like:
     * <pre>
     *  "sport-news-channel15" : "San Francisco 49er won last game"
     * </pre>
     */
    public Map<String, Object> getUserData() {
        return userData;
    }

    public void setUserData(Map<String, Object> userData) {
        this.userData = userData;
    }


    /**
     * Returns the SimplePush specific version number.
     */
    public String getSimplePush() {
        return simplePush;
    }

    public void setSimplePush(String simplePush) {
        this.simplePush = simplePush;
    }

    /**
     * Used for ADM Payload when used for "sync" Push messages.
     * Not supported on other platforms.
     *
     * @return the consolidation key
     */
    public String getConsolidationKey() {
        return consolidationKey;
    }

    public void setConsolidationKey(String consolidationKey) {
        this.consolidationKey = consolidationKey;
    }

    /**
     * Apns specific parameters to configure how the message will be displayed.
     */
    public APNs getApns() {
        return apns;
    }

    public void setApns(APNs apns) {
        this.apns = apns;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Message{" +
                ", alert='" + alert + '\'' +
                ", sound='" + sound + '\'' +
                ", badge=" + badge +
                ", priority=" + priority +
                ", consolidationKey=" + consolidationKey +
                ", user-data=" + userData +
                ", simple-push='" + simplePush + '\'' +
                '}';
    }
}
