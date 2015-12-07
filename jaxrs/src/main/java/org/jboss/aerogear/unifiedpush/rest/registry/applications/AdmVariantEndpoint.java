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
import org.jboss.aerogear.unifiedpush.api.AdmVariant;
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

@Path("/applications/{pushAppID}/adm")
public class AdmVariantEndpoint extends AbstractVariantEndpoint {

    /**
     * Add ADM Variant
     *
     * @param admVariant        new {@link AdmVariant}
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  created {@link AdmVariant}
     *
     * @statuscode 201 The ADM Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.AdmVariant")
    public Response registerAdmVariant(
            AdmVariant admVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
        }

        // some validation
        try {
            validateModelClass(admVariant);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // store the Adm variant:
        variantService.addVariant(admVariant);
        // add Adm variant, and merge:
        pushAppService.addVariant(pushApp, admVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(admVariant.getVariantID())).build()).entity(admVariant).build();
    }

    /**
     * List ADM Variants for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  list of {@link AdmVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.util.Set<org.jboss.aerogear.unifiedpush.api.AdmVariant>")
    public Response listAllAdmVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, AdmVariant.class)).build();
    }

    /**
     * Update ADM Variant
     *
     * @param id                    id of {@link PushApplication}
     * @param androidID             id of {@link AdmVariant}
     * @param updatedAdmApplication new info of {@link AdmVariant}
     *
     * @statuscode 204 The ADM Variant updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested ADM Variant resource does not exist
     */
    @PUT
    @Path("/{admID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.lang.Void")
    public Response updateAndroidVariation(
            @PathParam("pushAppID") String id,
            @PathParam("admID") String androidID,
            AdmVariant updatedAdmApplication) {

        AdmVariant admVariant = (AdmVariant) variantService.findByVariantID(androidID);
        if (admVariant != null) {

            // some validation
            try {
                validateModelClass(updatedAdmApplication);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // apply updated data:
            admVariant.setClientId(updatedAdmApplication.getClientId());
            admVariant.setClientSecret(updatedAdmApplication.getClientSecret());
            admVariant.setName(updatedAdmApplication.getName());
            admVariant.setDescription(updatedAdmApplication.getDescription());
            variantService.updateVariant(admVariant);
            return Response.ok(admVariant).build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }
}
