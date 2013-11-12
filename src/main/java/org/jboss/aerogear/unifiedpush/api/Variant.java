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

import java.util.Set;

import org.jboss.aerogear.unifiedpush.model.InstallationImpl;

/**
 * Logical construct which matches a mobile app in the appstore. 
 */
public interface Variant {

    /**
     * The type (Android, iOS or SimplePush) of the underlying variant.
     */
    VariantType getType();

    /**
     * The name of the variant (e.g. the name of the matching App in the Appstore) 
     */
    void setName(final String name);

    String getName();

    /**
     * Some description of the app.
     */
    void setDescription(final String description);

    String getDescription();

    /**
     * Identifier used to register an {@link Installation} with this Variant
     */
    void setVariantID(String variantID);

    String getVariantID();

    /**
     * The Variant-specific "secret", used from an {@link Installation} to register themselves against this Variant.
     */
    void setSecret(String secret);

    String getSecret();

    /**
     * The collection of {@link Installation}s for this Variant. 
     */
    void setInstallations(final Set<InstallationImpl> installations);

    Set<InstallationImpl> getInstallations();

}
