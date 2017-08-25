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

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;

import java.util.Map;


public interface PushApplicationService {

    /**
     * Store a new PushApplication object on the database.
     *
     * @param pushApp the push application object
     */
    void addPushApplication(PushApplication pushApp) throws IllegalArgumentException;

    /**
     * Performs an update/merge on the given entity.
     *
     * @param pushApp the push application object
     */
    void updatePushApplication(PushApplication pushApp);

    /**
     * Returns the PushApplication entity, matching the given ID.
     *
     * @param pushApplicationID push application ID
     *
     * @return push application entity
     */
    PushApplication findByPushApplicationID(String pushApplicationID);

    /**
     * Removes the given PushApplication entity.
     *
     * @param pushApp the push application object
     */
    void removePushApplication(PushApplication pushApp);

    /**
     * Registers the given Variant object with the given PushApplication.
     *
     * @param pushApp the push application object
     * @param variant the variant
     */
    void addVariant(PushApplication pushApp, Variant variant);

    Map<String, Long> countInstallationsByType(String pushApplicationID);

}
