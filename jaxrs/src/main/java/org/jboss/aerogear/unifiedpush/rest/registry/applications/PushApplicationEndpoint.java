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
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import java.util.Map;
import java.util.UUID;

@Stateless
@TransactionAttribute
@Path("/applications")
public class PushApplicationEndpoint extends AbstractBaseEndpoint {
    private static final int MAX_PAGE_SIZE = 25;
    private static final int DEFAULT_PAGE_SIZE = 8;

    @Inject
    private PushApplicationService pushAppService;

    // CREATE
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerPushApplication(PushApplication pushApp) {

         // some validation
        try {
            validateModelClass(pushApp);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        pushAppService.addPushApplication(pushApp);

        return Response.created(UriBuilder.fromResource(PushApplicationEndpoint.class).path(String.valueOf(pushApp.getPushApplicationID())).build()).entity(pushApp)
                .build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllPushApplications(@QueryParam("page") Integer page,
                                            @QueryParam("per_page") Integer pageSize) {
        if (pageSize != null) {
            pageSize = Math.min(MAX_PAGE_SIZE, pageSize);
        } else {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        if (page == null) {
            page = 0;
        }

        final PageResult<PushApplication> pageResult = searchService.findAllPushApplicationsForDeveloper(page, pageSize);
        return Response.ok(pageResult.getResultList()).header("total", pageResult.getCount()).build();
    }

    @GET
    @Path("/{pushAppID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("pushAppID") String pushApplicationID) {

        PushApplication pushApp = searchService.findByPushApplicationIDForDeveloper(pushApplicationID);
        if (pushApp != null) {
            return Response.ok(pushApp).build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    // UPDATE
    @PUT
    @Path("/{pushAppID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePushApplication(@PathParam("pushAppID") String pushApplicationID, PushApplication updatedPushApp) {

        PushApplication pushApp = searchService.findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp != null) {

            // some validation
            try {
                validateModelClass(updatedPushApp);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // update name/desc:
            pushApp.setDescription(updatedPushApp.getDescription());
            pushApp.setName(updatedPushApp.getName());
            pushAppService.updatePushApplication(pushApp);

            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    // UPDATE (MasterSecret Reset)
    @PUT
    @Path("/{pushAppID}/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetMasterSecret(@PathParam("pushAppID") String pushApplicationID) {

        //PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, extractUsername(request));
        PushApplication pushApp = searchService.findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp != null) {
            // generate the new 'masterSecret' and apply it:
            String newMasterSecret = UUID.randomUUID().toString();
            pushApp.setMasterSecret(newMasterSecret);
            pushAppService.updatePushApplication(pushApp);

            return Response.ok(pushApp).build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    // DELETE
    @DELETE
    @Path("/{pushAppID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePushApplication(@PathParam("pushAppID") String pushApplicationID) {

        PushApplication pushApp = searchService.findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp != null) {
            pushAppService.removePushApplication(pushApp);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
    }

    @GET
    @Path("/{pushAppID}/count")
    public Response countInstallations(@PathParam("pushAppID") String pushApplicationID) {

        Map<String, Long> result = pushAppService.countInstallationsByType(pushApplicationID);

        return Response.ok(result).build();
    }
}
