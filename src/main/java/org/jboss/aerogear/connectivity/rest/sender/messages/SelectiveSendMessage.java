/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.jboss.aerogear.connectivity.rest.sender.messages;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SelectiveSendMessage extends LinkedHashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public List<String> getAliases() {
        return (List<String>) this.get("alias");
    }

    @SuppressWarnings("unchecked")
    public List<String> getDeviceTypes() {
        return (List<String>) this.get("deviceType");
    }

    @SuppressWarnings("unchecked")
    public Map<String, ? extends Object> getMessage() {
        return (Map<String, ? extends Object>) this.get("message");
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getSimplePush() {
        return (Map<String, String>) this.getMessage().get("simple-push");
    }

}
