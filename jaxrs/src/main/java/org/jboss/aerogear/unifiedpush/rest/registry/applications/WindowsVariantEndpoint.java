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

import org.jboss.aerogear.unifiedpush.api.*;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/applications/{pushAppID}/windows{type}")
public class WindowsVariantEndpoint extends AbstractVariantEndpoint {

    /**
     * Add Windows Variant
     *
     * @param windowsVariant    new {@link WindowsVariant}
     * @param pushApplicationID id of {@link PushApplication}
     * @param uriInfo           the uri
     *
     * @return                  created {@link WindowsVariant}
     *
     * @statuscode 201 The Windows Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerWindowsVariant(
            WindowsVariant windowsVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
        }

        // some validation
        try {
            validateModelClass(windowsVariant);
        } catch (ConstraintViolationException cve) {
            logger.trace("Unable to create Windows variant");

            // Build and return the 400 (Bad Request) response
            Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        logger.trace("Register Windows variant with Push Application '{}'", pushApplicationID);
        // store the Windows variant:
        variantService.addVariant(windowsVariant);
        // add iOS variant, and merge:
        pushAppService.addVariant(pushApp, windowsVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(windowsVariant.getVariantID())).build()).entity(windowsVariant).build();
    }

    /**
     * List Windows Variants for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  list of {@link WindowsVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllWindowsVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, WindowsVariant.class)).build();
    }

    /**
     * Update Windows Variant
     *
     * @param windowsID             id of {@link WindowsVariant}
     * @param updatedWindowsVariant new info of {@link WindowsVariant}
     *
     * @return                     updated {@link WindowsVariant}
     *
     * @statuscode 200 The Windows Variant updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested Windows Variant resource does not exist
     */
    @PUT
    @Path("/{windowsID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateWindowsVariation(
            @PathParam("windowsID") String windowsID,
            WindowsVariant updatedWindowsVariant) {

        WindowsVariant windowsVariant = (WindowsVariant) variantService.findByVariantID(windowsID);
        if (windowsVariant != null) {

            // some validation
            try {
                validateModelClass(updatedWindowsVariant);
            } catch (ConstraintViolationException cve) {
                logger.info("Unable to update Windows Variant '{}'", windowsVariant.getVariantID());
                logger.debug("Details: {}", cve);

                // Build and return the 400 (Bad Request) response
                Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // apply updated data:
            if (windowsVariant instanceof WindowsWNSVariant) {
                WindowsWNSVariant windowsWNSVariant = (WindowsWNSVariant) windowsVariant;
                windowsWNSVariant.setClientSecret(((WindowsWNSVariant)updatedWindowsVariant).getClientSecret());
                windowsWNSVariant.setSid(((WindowsWNSVariant)updatedWindowsVariant).getSid());
            }
            windowsVariant.setName(updatedWindowsVariant.getName());
            windowsVariant.setDescription(updatedWindowsVariant.getDescription());

            logger.trace("Updating Windows Variant '{}'", windowsVariant.getVariantID());

            variantService.updateVariant(windowsVariant);
            return Response.ok(windowsVariant).build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    /**
     * Get the Windows Variant for the given ID
     *
     * @param variantId id of {@link Variant}
     * @return          requested {@link Variant}
     *
     * @statuscode 404 The requested Windows Variant resource does not exist
     */
    @GET
    @Path("/{variantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findVariantById(@PathParam("variantId") String variantId) {
        return doFindVariantById(variantId, WindowsVariant.class);
    }

    /**
     * Delete the Windows Variant
     *
     * @param variantId id of {@link Variant}
     *
     * @return no content or 404
     *
     * @statuscode 204 The Variant successfully deleted
     * @statuscode 404 The requested Windows Variant resource does not exist
     */
    @DELETE
    @Path("/{variantId}")
    public Response deleteVariant(@PathParam("variantId") String variantId) {
        return this.doDeleteVariant(variantId, WindowsVariant.class);
    }

    /**
     * Reset secret of the given Windows Variant
     *
     * @param variantId id of {@link Variant}
     * @return          {@link Variant} with new secret
     *
     * @statuscode 200 The secret of Windows Variant reset successfully
     * @statuscode 404 The requested Windows Variant resource does not exist
     */
    @PUT
    @Path("/{variantId}/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetSecret(@PathParam("variantId") String variantId) {
        return doResetSecret(variantId, WindowsVariant.class);
    }

}
