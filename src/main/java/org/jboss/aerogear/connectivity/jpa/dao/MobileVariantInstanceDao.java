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
package org.jboss.aerogear.connectivity.jpa.dao;

import java.util.List;

import org.jboss.aerogear.connectivity.jpa.GenericDao;
import org.jboss.aerogear.connectivity.model.MobileVariantInstanceImpl;

public interface MobileVariantInstanceDao extends GenericDao<MobileVariantInstanceImpl, String> {

    /**
     * Loads all installations with the same token for the given Variant
     */
    List<MobileVariantInstanceImpl> findMobileVariantInstancesForVariantByToken(String variantID, String deviceToken);

    /**
     * Broadcast/Selective Sender API:
     * 
     * Query all tokens for the given variant, by respecting a few criterias (category, aliases and deviceTypes)
     */
    List<String> findAllDeviceTokenForVariantIDByCategoryAndAliasAndDeviceType(String variantID, String category, List<String> aliases, List<String> deviceTypes);
}
