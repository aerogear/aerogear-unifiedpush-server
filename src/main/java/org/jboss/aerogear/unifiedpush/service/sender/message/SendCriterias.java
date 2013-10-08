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

import java.util.List;
import java.util.Map;

/**
 * Simple utility class, containing all "query criteria" options for a message,
 * that has been sent to the Send-HTTP endpoint.
 * 
 * <p>
 * For details have a look at the <a href="http://aerogear.org/docs/specs/aerogear-push-messages/">Message Format Specification</a>.
 */
public class SendCriterias {

    private final List<String> aliases;
    private final List<String> deviceTypes;
    private final String category;
    private final List<String> variants;

    @SuppressWarnings("unchecked")
    public SendCriterias(Map<String, Object> data) {
        this.aliases = (List<String>) data.remove("alias");
        this.deviceTypes = (List<String>) data.remove("deviceType");
        this.category = (String) data.remove("category");
        this.variants = (List<String>) data.remove("variants");
    }

    /**
     * Returns a list of user names or email addresses that will receive
     * a push notification.
     */
    public List<String> getAliases() {
        return aliases;
    }

    /**
     * Returns a list of device types that will receive a push notification. 
     */
    public List<String> getDeviceTypes() {
        return deviceTypes;
    }

    /**
     * Returns a category that will receive a push notification. 
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns a list of variant IDs that will receive a push notification. 
     */
    public List<String> getVariants() {
        return variants;
    }

    @Override
    public String toString() {
        return "[aliases=" + aliases + ", deviceTypes=" + deviceTypes + ", category=" + category + ", variants=" + variants + "]";
    }
}
