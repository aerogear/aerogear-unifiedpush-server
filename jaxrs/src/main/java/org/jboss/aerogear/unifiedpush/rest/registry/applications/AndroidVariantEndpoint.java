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

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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

import org.jboss.aerogear.security.authz.Secure;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

@Stateless
@TransactionAttribute
@Path("/applications/{pushAppID}/android")
@Secure( { "developer", "admin" })
public class AndroidVariantEndpoint extends AbstractVariantEndpoint {

    // new Android
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerAndroidVariant(
            AndroidVariant androidVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
        }

        // some validation
        try {
            validateModelClass(androidVariant);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // store the "developer:
        androidVariant.setDeveloper(loginName.get());

        // store the Android variant:
        variantService.addVariant(androidVariant);
        // add iOS variant, and merge:
        pushAppService.addAndroidVariant(pushApp, androidVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(androidVariant.getVariantID())).build()).entity(androidVariant).build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllAndroidVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        return Response.ok(pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get()).getAndroidVariants()).build();
    }

    // UPDATE
    @PUT
    @Path("/{androidID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAndroidVariation(
            @PathParam("pushAppID") String id,
            @PathParam("androidID") String androidID,
            AndroidVariant updatedAndroidApplication) {

        AndroidVariant androidVariant = (AndroidVariant) variantService.findByVariantIDForDeveloper(androidID, loginName.get());
        if (androidVariant != null) {

            // some validation
            try {
                validateModelClass(updatedAndroidApplication);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // apply updated data:
            androidVariant.setGoogleKey(updatedAndroidApplication.getGoogleKey());
            androidVariant.setProjectNumber(updatedAndroidApplication.getProjectNumber());
            androidVariant.setName(updatedAndroidApplication.getName());
            androidVariant.setDescription(updatedAndroidApplication.getDescription());
            variantService.updateVariant(androidVariant);
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    // DELETE
    @DELETE
    @Path("/{androidID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAllAndroidVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID, @PathParam("androidID") String androidID) {
        AndroidVariant androidVariant = (AndroidVariant) variantService.findByVariantIDForDeveloper(androidID, loginName.get());
        if (androidVariant == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
        }
        variantService.removeVariant(androidVariant);
        return Response.noContent().build();
    }
}
