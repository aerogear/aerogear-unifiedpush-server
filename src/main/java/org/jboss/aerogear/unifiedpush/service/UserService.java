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


import org.jboss.aerogear.unifiedpush.model.token.Credential;
import org.jboss.aerogear.unifiedpush.users.Developer;
import org.picketlink.idm.model.basic.User;

import java.util.List;

/**
 * Service class that offers functionality around the user management.
 */
public interface UserService {

    /**
     * Logs the developer into the system
     *
     * @param developer that contains the credentials
     * @return logged in developer with some extra properties set
     */
    Developer login(Developer developer);

    /**
     * Logs out the current authenticated developer
     */
    void logout();

    /**
     * Updates the developer to set a new password or change his role.
     *
     * @param developer to be updated
     */
    void updateUserPasswordAndRole(Developer developer);

    /**
     * Creates a new developer into the system.
     * No password is affected to this developer and by default the developer is disabled.
     * The returned developer contains a registrationLink property that has to be used by the developer to confirm
     * his registration.
     *
     * @param developer that contains the loginName and the role
     * @return developer in disabled state and with registrationLink
     */
    Developer enroll(Developer developer);

    /**
     * confirm the registration of a user. If successful, the developer will be enabled.
     *
     * @param credential object containing, along the loginName and password, the token
     */
    void confirm(Credential credential);

    /**
     * Disable the developer and generate a new token for the developer
     * @param developer to be reset.
     * @return developer in disabled state and with registrationLink
     */
    Developer reset(Developer developer);

    /**
     * Finds the developer by id
     *
     * @param id of the developer
     * @return a developer
     */
    Developer findById(String id);

    /**
     * Retrieves all the developers.
     * @return a list containing the developers
     */
    List<Developer> listAll();

    /**
     * Delete a developer by id
     *
     * @param id of the developer to be deleted.
     */
    void deleteById(String id);

    /**
     * Finds a developer by login name.
     *
     * @return User
     */
    Developer findUserByLoginName(String loginName);

    String getRoleByLoginName(String loginName);

    String getLoginName();

}
