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
import org.jboss.aerogear.unifiedpush.api.WindowsVariant;
import org.jboss.aerogear.unifiedpush.api.WindowsWNSVariant;

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

@Path("/applications/{pushAppID}/windows{type}")
public class WindowsVariantEndpoint extends AbstractVariantEndpoint {

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

            // Build and return the 400 (Bad Request) response
            Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // store the Windows variant:
        variantService.addVariant(windowsVariant);
        // add iOS variant, and merge:
        pushAppService.addVariant(pushApp, windowsVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(windowsVariant.getVariantID())).build()).entity(windowsVariant).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllWindowsVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, WindowsVariant.class)).build();
    }

    // UPDATE
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
            variantService.updateVariant(windowsVariant);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

}
