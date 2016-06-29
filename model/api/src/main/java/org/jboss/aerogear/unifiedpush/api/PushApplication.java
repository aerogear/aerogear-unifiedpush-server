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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Logical construct of an overall mobile and push-enabled Application
 */
public class PushApplication extends BaseModel {
    private static final long serialVersionUID = 6507691362454032282L;

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    @Size(min = 1, max = 255)
    private String description;

    /**
     * why having this if we already have an id?
     *
     * look at it like an API key, which is subject to change
     * TODO: improve naming?
     */
    private String pushApplicationID = UUID.randomUUID().toString();
    private String masterSecret = UUID.randomUUID().toString();

    @Size(min = 1, max = 255)
    private String developer;

    private List<Variant> variants = new ArrayList<>();

    /**
     * The name of the application.
     *
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * The description of the app.
     *
     * @param description the description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Identifier used to register variants with this PushApplication
     *
     * @param pushApplicationID the pushApplicationID
     */
    public void setPushApplicationID(String pushApplicationID) {
        this.pushApplicationID = pushApplicationID;
    }

    public String getPushApplicationID() {
        return pushApplicationID;
    }

    /**
     * The master password, used for sending message to a {@link PushApplication} or its {@link Variant}s.
     *
     * @param masterSecret the masterSecret
     */
    public void setMasterSecret(String masterSecret) {
        this.masterSecret = masterSecret;
    }

    public String getMasterSecret() {
        return masterSecret;
    }


    public List<Variant> getVariants() {
        return variants;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

    /**
     * The developer which created the app.
     *
     * @return the owning developer
     */
    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

}
