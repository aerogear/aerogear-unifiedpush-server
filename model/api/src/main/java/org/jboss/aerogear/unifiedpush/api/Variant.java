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
import java.util.UUID;

/**
 * Logical construct which matches a mobile app in the appstore.
 */
public abstract class Variant extends BaseModel {
    private static final long serialVersionUID = -5028062942838899201L;

    @NotNull
    @Size(min = 1, max = 255)
    private String name;

    @Size(min = 0, max = 255)
    private String description;

    private String variantID = UUID.randomUUID().toString();

    private String secret = UUID.randomUUID().toString();

    private String developer;

    /**
     * The @VariantType of the underlying variant.
     *
     * @return the variant type
     */
    public abstract VariantType getType();

    //ugly way to make it a immutable property
    public void setType(VariantType type) {}

    public String getName() {
        return this.name;
    }

    /**
     * The name of the variant (e.g. the name of the matching App in the Appstore)
     *
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * Some description of the app.
     *
     * @param description the description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    public String getVariantID() {
        return variantID;
    }

    /**
     * Identifier used to register an {@link Installation} with this Variant
     *
     * @param variantID the variant ID
     */
    public void setVariantID(String variantID) {
        this.variantID = variantID;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getSecret() {
        return secret;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }
}
