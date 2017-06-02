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
import org.jboss.aerogear.unifiedpush.api.FirefoxVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

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
import javax.ws.rs.core.UriInfo;

@Path("/applications/{pushAppID}/firefox")
public class FirefoxVariantEndpoint extends AbstractVariantEndpoint {

    /**
     * Add Firefox Variant
     *
     * @param firefoxVariant    new {@link FirefoxVariant}
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  created {@link FirefoxVariant}
     *
     * @statuscode 201 The Firefox Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.FirefoxVariant")
    public Response registerFirefoxVariant(
            FirefoxVariant firefoxVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Could not find requested PushApplicationEntity")
                    .build();
        }

        // some validation
        try {
            validateModelClass(firefoxVariant);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // store the Firefox variant:
        variantService.addVariant(firefoxVariant);
        // add Firefox variant, and merge:
        pushAppService.addVariant(pushApp, firefoxVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(firefoxVariant.getVariantID()).build())
                .entity(firefoxVariant)
                .build();
    }

    /**
     * List Firefox Variants for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  list of {@link FirefoxVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.util.Set<org.jboss.aerogear.unifiedpush.api.FirefoxVariant>")
    public Response listAllFirefoxVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, FirefoxVariant.class)).build();
    }

    /**
     * Update Firefox Variant
     *
     * @param id                        id of {@link PushApplication}
     * @param firefoxID                 id of {@link FirefoxVariant}
     * @param updatedFirefoxApplication new info of {@link FirefoxVariant}
     *
     * @statuscode 200 The Firefox Variant updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested Firefox Variant resource does not exist
     */
    @PUT
    @Path("/{firefoxID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.FirefoxVariant")
    public Response updateFirefoxVariant(
            @PathParam("pushAppID") String id,
            @PathParam("firefoxID") String firefoxID,
            FirefoxVariant updatedFirefoxApplication) {

        FirefoxVariant firefoxVariant = (FirefoxVariant) variantService.findByVariantID(firefoxID);
        if (firefoxVariant != null) {

            // some validation
            try {
                validateModelClass(updatedFirefoxApplication);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // apply updated data:
            firefoxVariant.setName(updatedFirefoxApplication.getName());
            firefoxVariant.setDescription(updatedFirefoxApplication.getDescription());
            variantService.updateVariant(firefoxVariant);
            return Response.ok(firefoxVariant).build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }
}
