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
package org.jboss.aerogear.unifiedpush.service;

import org.jboss.aerogear.unifiedpush.api.Variant;

/**
 * Service class that offers functionality to deal with the different variants (e.g. Android, iOS or SimplePush variants).
 */
public interface GenericVariantService {

    /**
     * Store a new Variant object on the database.
     *
     * @param variant the variant
     */
    void addVariant(Variant variant);

    /**
     * Performs an update/merge on the given entity.
     *
     * @param variant the variant
     */
    void updateVariant(Variant variant);

    /**
     * Returns the Variant entity, matching the given variantID.
     *
     * @param variantID the id
     *
     * @return the variant
     */
    Variant findByVariantID(String variantID);

    /**
     * Removes the given variant entity.
     *
     * @param variant the variant
     */
    void removeVariant(Variant variant);

}
