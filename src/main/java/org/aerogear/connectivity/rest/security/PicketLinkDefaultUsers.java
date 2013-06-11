package org.aerogear.connectivity.rest.security;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Role;
import org.picketlink.idm.model.SimpleRole;
import org.picketlink.idm.model.SimpleUser;
import org.picketlink.idm.model.User;

@Singleton
@Startup
public class PicketLinkDefaultUsers {

    @Inject
    private IdentityManager identityManager;

    /**
     * <p>Loads some users during the first construction.</p>
     */
    //TODO this entire initialization code will be removed
    @PostConstruct
    public void create() {
System.out.println("\n\n\n\n\n\nJOOOOOfycker");
        User user = new SimpleUser("john");

        user.setEmail("john@doe.com");
        user.setFirstName("John");
        user.setLastName("Doe");

        /*
         * Note: Password will be encoded in SHA-512 with SecureRandom-1024 salt
         * See http://lists.jboss.org/pipermail/security-dev/2013-January/000650.html for more information
         */
        this.identityManager.add(user);
        this.identityManager.updateCredential(user, new Password("123"));

        Role roleDeveloper = new SimpleRole("simple");
        Role roleAdmin = new SimpleRole("admin");

        this.identityManager.add(roleDeveloper);
        this.identityManager.add(roleAdmin);

        identityManager.grantRole(user, roleDeveloper);
        identityManager.grantRole(user, roleAdmin);

    }

}
