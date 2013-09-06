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

package org.jboss.aerogear.unifiedpush.rest.security;

import org.jboss.aerogear.unifiedpush.users.Developer;
import org.jboss.aerogear.security.authz.IdentityManagement;
import org.jboss.aerogear.security.authz.Secure;
import org.jboss.aerogear.unifiedpush.users.UserRoles;
import org.picketlink.Identity;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.model.basic.User;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Stateless
@Path("/auth")
public class AdminEndpoint {

    @Inject
    private PartitionManager partitionManager;

    private IdentityManager identityManager;
    private RelationshipManager relationshipManager;

    @POST
    @Path("/enroll")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secure("admin")
    public Response enroll(Developer developer) {
        try {

            this.identityManager = partitionManager.createIdentityManager();
            this.relationshipManager = partitionManager.createRelationshipManager();
            User user = new User(developer.getLoginName());
            identityManager.add(user );
            Calendar calendar = expirationDate();
            Password password = new Password(developer.getPassword().toCharArray());

            identityManager.updateCredential(user , password, new Date(), calendar.getTime());

            Role developerRole= BasicModel.getRole(identityManager,UserRoles.DEVELOPER);

            grantRoles(user,developerRole);
            List<User> list = identityManager.createIdentityQuery(User.class)
                    .setParameter(User.LOGIN_NAME, user.getLoginName()).getResultList();
            user = list.get(0);
            developer.setId(user.getId());

        } catch (IdentityManagementException ime) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Credential not available").build();
        }

        return Response.ok(developer).build();

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
