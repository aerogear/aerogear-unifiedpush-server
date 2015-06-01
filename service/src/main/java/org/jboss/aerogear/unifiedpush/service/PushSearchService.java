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

import java.util.List;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dto.Count;
import org.jboss.aerogear.unifiedpush.service.dashboard.Application;
import org.jboss.aerogear.unifiedpush.service.dashboard.ApplicationVariant;
import org.jboss.aerogear.unifiedpush.service.dashboard.DashboardData;

/**
 * Base of the implementation for the admin/developer view
 */
public interface PushSearchService {

    /**
     * Finder that returns all pushApplication object for the given owner/developer.
     *
     * @param page the actual page for the pagination
     * @param pageSize number of push applications per page
     *
     * @return list of push applications
     */
    PageResult<PushApplication, Count> findAllPushApplicationsForDeveloper(Integer page, Integer pageSize);

    /**
     * Finder that returns an actual PushApplication, identified by its ID and its owner/developer.
     *
     * @param pushApplicationID the push application id
     *
     * @return push application entity
     */
    PushApplication findByPushApplicationIDForDeveloper(String pushApplicationID);

    /**
     * See that variant exists for loggedin developer
     *
     * @param variantID the variant ID
     *
     * @return does the variant exist for the logged in user?
     */
    boolean existsVariantIDForDeveloper(String variantID);

    /**
     * Receives the dashboard data for the given user
     *
     * @return object containing the dashboard data
     */
    DashboardData loadDashboardData();

    /**
     * Loads all the Variant objects where we did notice some failures on sending
     * for the given user.
     *
     * @return list of application variant combinations
     */
    List<ApplicationVariant> getVariantsWithWarnings();

    /**
     * Loads all the Variant objects with the most recent received messages
     *
     * @param maxResults number of max results on the result
     *
     * @return list of applications
     */
    List<Application> getLatestActivity(int maxResults);


    /**
     * Find all installations for the variant specified.
     *
     * @param variantID the id of the variant to find the installations for
     * @param page the page number
     * @param pageSize the size of the pages
     *
     * @return page result containing the list plus a total number of rows
     */
    PageResult<Installation, Count> findAllInstallationsByVariantForDeveloper(String variantID, Integer page, Integer pageSize, String search);


}
