/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.message.apns;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;

/**
 * iOS specific settings for Push Notifications
 */
@JsonRootName("apns")
public class APNs implements Serializable {

    private static final long serialVersionUID = 1721248899064332647L;

    @JsonProperty("action-category")
    private String actionCategory;
    private String title;
    private String action;

    @JsonProperty("localized-key")
    private String localizedKey;

    @JsonProperty("localized-arguments")
    private String[] localizedArguments;

    @JsonProperty("localized-title-key")
    private String localizedTitleKey;

    @JsonProperty("localized-title-arguments")
    private String[] localizedTitleArguments;

    @JsonProperty("url-args")
    private String[] urlArgs;

    @JsonProperty("content-available")
    private boolean contentAvailable;

    @JsonProperty("mutable-content")
    private boolean mutableContent;

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
     * Returns the value of the 'title' key from the submitted payload.
     * This key is recognized in APNs for Safari, without any API invocation.
     *
     */
    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    /**
     * Returns the value of the 'action' key from the submitted payload.
     * This key is recognized in APNs for Safari.
     *
     */
    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }

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
     * Returns the value of the 'mutable-content' key from the submitted payload.
     * This key was introduced in iOS 10 and indicates if the remote notification’s content should be modified.
     */
    public boolean hasMutableContent() {
        return mutableContent;
    }

    public void setMutableContent(boolean mutableContent) {
        this.mutableContent = mutableContent;
    }

    /**
     * Returns the value of the 'url-args' key from the submitted payload.
     * This key is recognized in APNs for Safari.
     *
     */
    public String[] getUrlArgs() { return urlArgs; }

    public void setUrlArgs(String[] urlArgs) { this.urlArgs = urlArgs; }

    /**
     * The key to a title string in the Localizable.strings file for the current localization.
     */
    public String getLocalizedTitleKey() { return localizedTitleKey;}

    public void setLocalizedTitleKey(String localizedTitleKey) {this.localizedTitleKey = localizedTitleKey;}

    /**
     * Sets the arguments for the localizable title key
     */
    public String[] getLocalizedTitleArguments() { return localizedTitleArguments;}

    public void setLocalizedTitleArguments(String[] localizedTitleArguments) {this.localizedTitleArguments = localizedTitleArguments;}

    public String getLocalizedKey() { return localizedKey; }

    public void setLocalizedKey(String localizedKey) { this.localizedKey = localizedKey; }

    public String[] getLocalizedArguments() { return localizedArguments; }

    public void setLocalizedArguments(String[] localizedArguments){ this.localizedArguments = localizedArguments; }
}
