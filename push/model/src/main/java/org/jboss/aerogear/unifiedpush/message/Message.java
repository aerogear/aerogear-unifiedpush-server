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


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * The message part of the UnifiedPush message.
 *
 * <p>
 * For details have a look at the <a href="http://aerogear.org/docs/specs/aerogear-push-messages/">Message Format Specification</a>.
 */
public class Message {
    @JsonProperty("action-category")
    private String actionCategory;
    private String alert;
    private String title;
    private String action;
    private String sound;

    @JsonProperty("url-args")
    private String[] urlArgs;

    @JsonProperty("content-available")
    private boolean contentAvailable;
    private int badge = -1;

    @JsonProperty("user-data")
    private Map<String, Object> userData = new HashMap<String, Object>();

    @JsonProperty("simple-push")
    private String simplePush;

    /**
     * Returns the value of the 'action-category', which is used on the client (iOS for now),
     * to invoke a certain "user action" on the device, based on the push message. Implemented for iOS8
     */
    public String getActionCategory() {
        return actionCategory;
    }

    public void setActionCategory(String actionCategory) {
        this.actionCategory = actionCategory;
    }

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
     * Returns the value of the 'title' key from the submitted payload.
     * This key is recognized in APNs for Safari, without any API invocation and
     * on AeroGear's GCM offerings.
     *
     */
    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    /**
     * Returns the value of the 'action' key from the submitted payload.
     * This key is recognized in APNs for Safari, without any API invocation and
     * on AeroGear's GCM offerings.
     *
     */
    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }

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
     * Used for in iOS specific feature, to indicate if content (for Newsstand or silent messages) has marked as
     * being available
     *
     * Not supported on other platforms.
     */
    public boolean isContentAvailable() {
        return contentAvailable;
    }

    public void setContentAvailable(boolean contentAvailable) {
        this.contentAvailable = contentAvailable;
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
     * Returns the value of the 'url-args' key from the submitted payload.
     * This key is recognized in APNs for Safari, without any API invocation and
     * on AeroGear's GCM offerings.
     *
     */
    public String[] getUrlArgs() { return urlArgs; }

    public void setUrlArgs(String[] urlArgs) { this.urlArgs = urlArgs; }

    /**
     * Returns the SimplePush specific version number.
     */
    public String getSimplePush() {
        return simplePush;
    }

    public void setSimplePush(String simplePush) {
        this.simplePush = simplePush;
    }

    @Override
    public String toString() {
        return "Message{" +
                "action-category='" + actionCategory + '\'' +
                ", alert='" + alert + '\'' +
                ", sound='" + sound + '\'' +
                ", contentAvailable=" + contentAvailable +
                ", badge=" + badge +
                ", user-data=" + userData +
                ", simple-push='" + simplePush + '\'' +
                '}';
    }
}
