/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.dao;

import org.jboss.aerogear.unifiedpush.api.Variant;

import java.util.List;

public interface VariantDao extends GenericBaseDao<Variant, String> {

    /**
     * Returns the Variant entity, matching the given variantID.
     */
    Variant findByVariantID(String variantID);

    /**
     * Finder that returns the actual variant, identified by its ID and its owner/developer.
     */
    Variant findByVariantIDForDeveloper(String variantID, String loginName);

    /**
     * Loads list of all variantIDs for given user.
     */
    List<String> findVariantIDsForDeveloper(String principalName);

    /**
     * Loads list of all variants, where IDs are given.
     */
    List<Variant> findAllVariantsByIDs(List<String> variantIDs);
}
