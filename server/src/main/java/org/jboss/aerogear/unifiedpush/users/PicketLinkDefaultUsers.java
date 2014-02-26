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
package org.jboss.aerogear.unifiedpush.users;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

@Singleton
@Startup
/**
 * Preload a default user into the database
 */
public class PicketLinkDefaultUsers {

    @Inject
    private PartitionManager partitionManager;

    private RelationshipManager relationshipManager;

    /**
     * <p>Loads some users during the <b>first</b> construction.</p>
     */
    @PostConstruct
    public void create() {

        this.relationshipManager = partitionManager.createRelationshipManager();

        IdentityManager identityManager = partitionManager.createIdentityManager();

        final String DEFAULT_PASSWORD = "123";
        final String DEFAULT_DEVELOPER = "developer";
        final String DEFAULT_ADMIN = "admin";

        User developerUser = BasicModel.getUser(identityManager, DEFAULT_DEVELOPER);

        // We only create the Developer user, if there is none;
        // if present, there is also no need to add the same 'Developer' user again.
        if (developerUser == null) {
            developerUser = new User(DEFAULT_DEVELOPER);
            identityManager.add(developerUser);

            Calendar calendar = expirationDate();
            Password password = new Password(DEFAULT_PASSWORD.toCharArray());

            identityManager.updateCredential(developerUser, password, new Date(), calendar.getTime());

            Role roleDeveloper = new Role(UserRoles.DEVELOPER);

            identityManager.add(roleDeveloper);

            grantRoles(developerUser, roleDeveloper);
        }

        //Temp hack to add user with admin rights
        User adminUser = BasicModel.getUser(identityManager, DEFAULT_ADMIN);

        // We only create the Admin user, if there is none;
        // if present, there is also no need to apply the same 'Admin' user again.
        if (adminUser == null) {
            adminUser = new User(DEFAULT_ADMIN);
            identityManager.add(adminUser);

            Calendar calendar = expirationDate();
            Password password = new Password(DEFAULT_PASSWORD.toCharArray());

            identityManager.updateCredential(adminUser, password, new Date(), calendar.getTime());

            Role roleAdmin = new Role(UserRoles.ADMIN);

            identityManager.add(roleAdmin);

            grantRoles(adminUser, roleAdmin);
        }

    }

    private void grantRoles(User user, Role role) {
        BasicModel.grantRole(relationshipManager, user, role);
    }

    //Expiration date of the password
    private Calendar expirationDate() {
        int EXPIRATION_TIME = -5;
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.add(Calendar.MINUTE, EXPIRATION_TIME);
        return expirationDate;
    }
}
