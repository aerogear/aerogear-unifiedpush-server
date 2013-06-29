/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.jboss.aerogear.connectivity.users;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;

@Singleton
@Startup
/**
 * Dummy Users
 */
public class PicketLinkDefaultUsers {

    @Inject
    private IdentityManager identityManager;

    /**
     * <p>Loads some users during the first construction.</p>
     */
    //TODO this entire initialization code will be removed
    @PostConstruct
    public void create() {

        // developers!! developers!! developers!! developers!!

        Developer admin = new Developer();
        admin.setLoginName("admin");

        /*
         * Note: Password will be encoded in SHA-512 with SecureRandom-1024 salt
         * See http://lists.jboss.org/pipermail/security-dev/2013-January/000650.html for more information
         */
        this.identityManager.add(admin);
        this.identityManager.updateCredential(admin, new Password("123"));

        Role roleDeveloper = new SimpleRole("developer");
        Role roleAdmin = new SimpleRole("admin");
        this.identityManager.add(roleDeveloper);
        this.identityManager.add(roleAdmin);
        identityManager.grantRole(admin, roleDeveloper);
        identityManager.grantRole(admin, roleAdmin);
    }
}
