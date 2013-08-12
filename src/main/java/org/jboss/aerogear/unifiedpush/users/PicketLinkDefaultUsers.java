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
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.User;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;

@Singleton
@Startup
/**
 * Dummy Users
 */
public class PicketLinkDefaultUsers {

    @Inject
    private IdentityManager identityManager;

    /**
     * <p>Loads some users during the <b>first</b> construction.</p>
     */
    //TODO this entire initialization code will be removed - https://issues.jboss.org/browse/AGPUSH-223
    @PostConstruct
    public void create() {

        User adminUser = identityManager.getUser("admin");

        // We only create the Admin, if there is none:
        if (adminUser == null) {

            Developer admin = new Developer();
            admin.setLoginName("admin");

            this.identityManager.add(admin);
            this.identityManager.updateCredential(admin, new Password("123"), new Date(), expirationDate());

            Role roleDeveloper = new SimpleRole(UserRoles.DEVELOPER);
            this.identityManager.add(roleDeveloper);
            identityManager.grantRole(admin, roleDeveloper);

        }
    }

    //Expiration date of the password
    private Date expirationDate() {
        Calendar expirationDate = Calendar.getInstance();
        expirationDate.add(Calendar.HOUR, -1);
        return expirationDate.getTime();
    }
}
