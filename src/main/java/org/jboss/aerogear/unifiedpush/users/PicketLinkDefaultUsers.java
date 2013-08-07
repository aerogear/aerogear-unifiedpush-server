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
import org.picketlink.idm.model.sample.Role;
import org.picketlink.idm.model.sample.SampleModel;
import org.picketlink.idm.model.sample.User;

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

    public static final String DEFAULT_PASSWORD = "123";
    public static final String DEFAULT_USER = "admin";
    public static final int EXPIRATION_TIME = -5;
    @Inject
    private IdentityManager identityManager;
    @Inject
    private PartitionManager partitionManager;

    /**
     * <p>Loads some users during the <b>first</b> construction.</p>
     */
    @PostConstruct
    public void create() {

        User adminUser = SampleModel.getUser(identityManager, DEFAULT_USER);

        // We only create the Admin, if there is none
        if (adminUser == null) {

            adminUser = new User(DEFAULT_USER);
            identityManager.add(adminUser);

            Password password = new Password(DEFAULT_PASSWORD.toCharArray());

            Calendar expirationDate = Calendar.getInstance();

            expirationDate.add(Calendar.MINUTE, EXPIRATION_TIME);

            identityManager.updateCredential(adminUser, password, new Date(), expirationDate.getTime());
            Role roleDeveloper = new Role(UserRoles.DEVELOPER);
            this.identityManager.add(roleDeveloper);
            RelationshipManager relationshipManager = partitionManager.createRelationshipManager();
            SampleModel.grantRole(relationshipManager, adminUser, roleDeveloper);
        }
    }
}
