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

import java.util.List;
import java.util.Map;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dto.Count;

public interface PushApplicationDao extends GenericBaseDao<PushApplication, String> {

    /**
     * Finder that returns all pushApplication object for the given owner/developer.
     *
     * @param loginName the username
     * @param page number of the page for pagination
     * @param pageSize the size of the page
     *
     * @return list of push applications
     */
    PageResult<PushApplication, Count> findAllForDeveloper(String loginName, Integer page, Integer pageSize);

    /**
     * Finder that returns a list, containing all pushApplication ids for the given owner/developer.
     *
     * @param loginName the username
     *
     * @return list of push application ids
     */
    List<String> findAllPushApplicationIDsForDeveloper(String loginName);

    /**
     * Finder that returns an actual PushApplicationEntity, identified by its ID and its owner/developer.
     *
     * @param loginName the username
     * @param pushApplicationID the push application id
     *
     * @return push application object or null
     */
    PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID, String loginName);

    /**
     * Finder that returns an actual PushApplicationEntity, identified by its ID.
     *
     * @param pushApplicationID the push application id
     *
     * @return push application object or null
     */
    PushApplication findByPushApplicationID(String pushApplicationID);

    /**
     * Finder that returns an actual PushApplication by its Name.
     *
     * @param name the push application name
     *
     * @return push application object or null
     */
    PushApplication findByPushApplicationName(String name);

    Map<String, Long> countInstallationsByType(String pushApplicationID);

    /**
     * Returns total number of PushApplications for given user
     *
     * @param name the username
     *
     * @return number of application for the given user
     */
    long getNumberOfPushApplicationsForDeveloper(String name);

    /**
     * Return push applications that are owning the given variants.
     *
     * @param variantIDs list of variant ids
     *
     * @return list of push applications\
     *
     */
    List<PushApplication> findByVariantIds(List<String> variantIDs);

    PushApplication findByVariantId(String variantId);

    //Admin queries
    /**
     * Finder that returns an actual PushApplicationEntity, identified by its ID and its owner/developer.
     *
     * @param pushApplicationID the push application id
     *
     * @return push application or null
     */
    PushApplication findAllByPushApplicationID(String pushApplicationID);

    PageResult<PushApplication, Count> findAll(Integer page, Integer pageSize);

    long getNumberOfPushApplicationsForDeveloper();
}
