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
package org.jboss.aerogear.unifiedpush.rest.user;

import org.jboss.aerogear.security.authz.Secure;
import org.jboss.aerogear.unifiedpush.service.UserService;
import org.jboss.aerogear.unifiedpush.users.Developer;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.query.IdentityQuery;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;

@Stateless
@TransactionAttribute
@Path("/users")
@Secure("admin")
public class UserEndpoint {

    @Inject
    private UserService userService;

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response findById(@PathParam("id") String id) {
        User developer = userService.findById(id);
        if (developer == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(developer).build();
    }

    @GET
    @Produces("application/json")
    public List<Developer> listAll() {
       return userService.listAll();
    }


    @DELETE
    @Path("/{id}")
    public Response deleteById(@PathParam("id") String id) {
        userService.deleteById(id);
        return Response.noContent().build();
    }

}
