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

package org.aerogear.connectivity.rest.registry.applications;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.aerogear.connectivity.cdi.interceptor.Secure;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.service.PushApplicationService;
import org.picketlink.Identity;

@Stateless
@TransactionAttribute
@Path("/applications")
public class PushApplicationEndpoint {

    @Inject private PushApplicationService pushAppService;
    @Inject private Identity identity;

    // CREATE
    @Secure({"admin"})
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerPushApplication(PushApplication pushApp) {
        // poor validation
        if (pushApp.getName() == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        // create ID...
        pushApp.setPushApplicationID(UUID.randomUUID().toString());
        pushAppService.addPushApplication(pushApp);

        return Response.created(UriBuilder.fromResource(PushApplicationEndpoint.class).path(String.valueOf(pushApp.getPushApplicationID())).build()).entity(pushApp).build();
    }
    
    // READ
    @Secure({"homer"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllPushApplications()  {
        // nope...
//        if (! identity.isLoggedIn()) {
//            return Response.status(Status.UNAUTHORIZED).build();
//        }

        return Response.ok(pushAppService.findAllPushApplications()).build();
    }

    @GET
    @Path("/{pushAppID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("pushAppID") String id) {
        PushApplication pushApp = pushAppService.findByPushApplicationID(id);
        
        if (pushApp!=null) {
            return Response.ok(pushApp).build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }

    // UPDATE
    @PUT
    @Path("/{pushAppID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePushApplication(@PathParam("pushAppID") String id, PushApplication updatedPushApp) {
        PushApplication pushApp = pushAppService.findByPushApplicationID(id);
        
        if (pushApp != null) {

            // poor validation
            if (pushApp.getName() == null) {
                return Response.status(Status.BAD_REQUEST).build();
            }

            // update name/desc:
            pushApp.setDescription(updatedPushApp.getDescription());
            pushApp.setName(updatedPushApp.getName());
            pushAppService.updatePushApplication(pushApp);

            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }

    // DELETE
    @DELETE
    @Path("/{pushAppID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deletePushApplication(@PathParam("pushAppID") String id) {
        PushApplication pushApp = pushAppService.findByPushApplicationID(id);
        
        if (pushApp != null) {
            pushAppService.removePushApplication(pushApp);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }   
}