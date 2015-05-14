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

import com.qmino.miredot.annotations.ReturnType;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.SimplePushVariant;

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

@Path("/applications/{pushAppID}/simplePush")
public class SimplePushVariantEndpoint extends AbstractVariantEndpoint {

    /**
     * Add SimplePush Variant
     *
     * @param simplePushVariant new {@link SimplePushVariant}
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  created {@link SimplePushVariant}
     *
     * @statuscode 201 The SimplePush Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.SimplePushVariant")
    public Response registerSimplePushVariant(
            SimplePushVariant simplePushVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

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

    /**
     * List SimplePush Variants for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  list of {@link SimplePushVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.util.Set<org.jboss.aerogear.unifiedpush.api.SimplePushVariant>")
    public Response listAllSimplePushVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {

        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, SimplePushVariant.class)).build();
    }

    /**
     * Update SimplePush Variant
     *
     * @param id                            id of {@link PushApplication}
     * @param simplePushID                  id of {@link SimplePushVariant}
     * @param updatedSimplePushApplication  new info of {@link SimplePushVariant}
     *
     * @statuscode 204 The SimplePush Variant updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested SimplePush Variant resource does not exist
     */
    @PUT
    @Path("/{simplePushID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.lang.Void")
    public Response updateSimplePushVariation(
            @PathParam("pushAppID") String id,
            @PathParam("simplePushID") String simplePushID,
            SimplePushVariant updatedSimplePushApplication) {

        SimplePushVariant spVariant = (SimplePushVariant) variantService.findByVariantID(simplePushID);
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
