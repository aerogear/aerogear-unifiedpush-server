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

import java.io.Serializable;
import java.util.List;

/**
 * Simple class, containing all "query criteria" options for a message,
 * that has been sent to the Send-HTTP endpoint.
 * <p>
 * For details have a look at the <a href="https://aerogear.org/docs/unifiedpush/push-message-format/">AeroGear UnifiedPush Message Format</a>.
 */
public class Criteria implements Serializable {

    private static final long serialVersionUID = -3393123279151223615L;

    @JsonProperty("alias")
    private List<String> aliases;
    @JsonProperty("deviceType")
    private List<String> deviceTypes;
    private List<String> categories;
    private List<String> variants;

    /**
     * Returns a list of user names or email addresses that will receive
     * a push notification.
     */
    public List<String> getAliases() {
        return aliases;
    }

    public void setAliases(List<String> aliases) {
        this.aliases = aliases;
    }

    /**
     * Returns a list of device types that will receive a push notification.
     */
    public List<String> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<String> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    /**
     * Returns a category that will receive a push notification.
     */
    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    /**
     * Returns a list of variant IDs that will receive a push notification.
     */
    public List<String> getVariants() {
        return variants;
    }

    public void setVariants(List<String> variants) {
        this.variants = variants;
    }

    @Override
    public String toString() {
        return "Criteria{" +
                "aliases=" + aliases +
                ", deviceTypes=" + deviceTypes +
                ", categories=" + categories +
                ", variants=" + variants +
                '}';
    }
}
