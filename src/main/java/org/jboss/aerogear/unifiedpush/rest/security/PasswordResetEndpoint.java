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

import org.jboss.aerogear.security.exception.AeroGearSecurityException;
import org.jboss.aerogear.unifiedpush.model.token.Credential;
import org.jboss.aerogear.unifiedpush.service.UserService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@Stateless
@Path("/")
public class PasswordResetEndpoint {

    @Inject
    private UserService userService;

    private static final Logger LOGGER = Logger.getLogger(PasswordResetEndpoint.class.getSimpleName());

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response confirm(Credential credential) {
        try {
          userService.confirm(credential);
          return Response.status(NO_CONTENT)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(NO_CONTENT).build();
        } catch (AeroGearSecurityException agse){
            return Response.status(NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/initreset/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reset( @PathParam("id") String id) {
        try {
            return Response.ok(userService.reset(userService.findById(id))).build();
        } catch (AeroGearSecurityException agse){
            return Response.status(NOT_FOUND).build();
        }
    }
}
