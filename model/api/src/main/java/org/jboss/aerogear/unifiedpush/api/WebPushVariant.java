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
package org.jboss.aerogear.unifiedpush.api;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * The WebPush variant class encapsulates specific behavior for different WebPush providers.
 */
public class WebPushVariant extends Variant {
    private static final long serialVersionUID = 1142847851407493017L;

    @NotNull
    @Size(min = 1, max = 255, message = "Server Key must be max. 255 chars long")
    private String fcmServerKey;

    @Size(max = 255, message = "Sender ID must be max. 255 chars long")
    private String fcmSenderID;

    @Size(max = 255, message = "Custom server URL must be max. 255 chars long")
    private String customServerUrl;

    /**
     * The Server Key from the Firebase Cloud Messaging Console of a project which has been enabled for FCM.
     *
     @return the Server key
     */
    public String getFcmServerKey() {
        return fcmServerKey;
    }

    public void setFcmServerKey(final String fcmServerKey) {
        this.fcmServerKey = fcmServerKey;
    }

    /**
     * The Sender ID from the Firebase Cloud Messaging Console is <i>not</i> needed for sending push messages,
     * but it is a convenience to "see" it on the Admin UI as well, since the web applications require it.
     *
     * @return the Sender ID
     */
    public String getFcmSenderID() {
        return fcmSenderID;
    }

    public void setFcmSenderID(final String fcmSenderID) {
        this.fcmSenderID = fcmSenderID;
    }

    /**
     * The URL to the custom WebPush Server, which could be used for WebPush notification in intranet
     * or for IoT devices.
     *
     * @return the custom server URL
     */
    public String getCustomServerUrl() {
        return customServerUrl;
    }

    public void setCustomServerUrl(String customServerUrl) {
        this.customServerUrl = customServerUrl;
    }

    @Override
    public VariantType getType() {
        return VariantType.WEB_PUSH;
    }
}
