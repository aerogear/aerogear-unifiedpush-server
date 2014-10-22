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
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;

import javax.ejb.Stateless;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

@Stateless
@Path("/applications/{pushAppID}/simplePush")
public class SimplePushVariantEndpoint extends AbstractVariantEndpoint {

    // new SimplePush
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerSimplePushVariant(
            SimplePushVariant simplePushVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = searchManager.getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
        }

        // some validation
        try {
            validateModelClass(simplePushVariant);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // store the SimplePush variant:
        variantService.addVariant(simplePushVariant);
        // add iOS variant, and merge:
        pushAppService.addVariant(pushApp, simplePushVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(simplePushVariant.getVariantID())).build()).entity(simplePushVariant).build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllSimplePushVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {

        final PushApplication application = searchManager.getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, SimplePushVariant.class)).build();
    }

    // UPDATE
    @PUT
    @Path("/{simplePushID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSimplePushVariation(
            @PathParam("pushAppID") String id,
            @PathParam("simplePushID") String simplePushID,
            SimplePushVariant updatedSimplePushApplication) {

        SimplePushVariant spVariant = (SimplePushVariant) searchManager.getSearch().findByVariantIDForDeveloper(simplePushID);
        if (spVariant != null) {

            // some validation
            try {
                validateModelClass(updatedSimplePushApplication);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // apply updated data:
            spVariant.setName(updatedSimplePushApplication.getName());
            spVariant.setDescription(updatedSimplePushApplication.getDescription());
            variantService.updateVariant(spVariant);
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

}
