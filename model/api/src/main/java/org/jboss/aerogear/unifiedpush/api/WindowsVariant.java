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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class WindowsVariant extends Variant {

    @NotNull
    @Size(max = 255)
    @JsonIgnore
    private String sid;

    @NotNull
    @Size(max = 255)
    @JsonIgnore
    private String clientSecret;


    /**
     * SID (Package security identifier) used to connect to the windows push notification services
     * @return the sid
     */
    @JsonIgnore
    public String getSid() {
        return sid;
    }

    @JsonProperty
    public void setSid(String sid) {
        this.sid = sid;
    }

    /**
     * The client secret (password) to connect to the windows push notification services
     * @return the client secret
     */
    @JsonIgnore
    public String getClientSecret() {
        return clientSecret;
    }

    @JsonProperty
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public VariantType getType() {
        return VariantType.WINDOWS;
    }
}
