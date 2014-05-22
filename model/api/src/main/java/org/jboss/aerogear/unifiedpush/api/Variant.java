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

import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Logical construct which matches a mobile app in the appstore.
 */
public abstract class Variant extends BaseModel {
    private static final long serialVersionUID = -5028062942838899201L;

    @Size(min = 1, max = 255)
    private String name;

    @Size(min = 0, max = 255)
    private String description;

    private String variantID = UUID.randomUUID().toString();

    private String secret = UUID.randomUUID().toString();

    private String developer;

    private Set<Installation> installations = new HashSet<Installation>();

    /**
     * The type (Android, iOS or SimplePush) of the underlying variant.
     */
    public abstract VariantType getType();

    public String getName() {
        return this.name;
    }

    /**
     * The name of the variant (e.g. the name of the matching App in the Appstore)
     */
    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    /**
     * Some description of the app.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    @JsonIgnore
    public Set<Installation> getInstallations() {
        return this.installations;
    }

    /**
     * The collection of {@link Installation}s for this Variant.
     */
    public void setInstallations(final Set<Installation> installations) {
        this.installations = installations;
    }

    public String getVariantID() {
        return variantID;
    }

    /**
     * Identifier used to register an {@link Installation} with this Variant
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
