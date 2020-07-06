/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.rest.annotations.DisabledByEnvironment;
import org.jboss.aerogear.unifiedpush.rest.util.error.ErrorBuilder;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

@Path("/applications/{pushAppID}/android")
@DisabledByEnvironment("android")
public class AndroidVariantEndpoint extends AbstractVariantEndpoint<AndroidVariant> {

    // required for RESTEasy
    public AndroidVariantEndpoint() {
        super(AndroidVariant.class);
    }

    // required for tests
    AndroidVariantEndpoint(Validator validator, SearchManager searchManager) {
        super(validator, searchManager, AndroidVariant.class);
    }

    /**
     * Add Android Variant
     *
     * @param androidVariant    new {@link AndroidVariant}
     * @param pushApplicationID id of {@link PushApplication}
     * @param uriInfo           the uri
     * @return created {@link AndroidVariant}
     *
     * @statuscode 201 The Android Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerAndroidVariant(
            AndroidVariant androidVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        // some validation
        try {
            validateModelClass(androidVariant);
        } catch (ConstraintViolationException cve) {
            logger.trace("Unable to create Android variant");
            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        logger.trace("Register Android variant with Push Application '{}'", pushApplicationID);
        // store the Android variant:
        variantService.addVariant(androidVariant);
        // add Android variant, and merge:
        pushAppService.addVariant(pushApp, androidVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(androidVariant.getVariantID())).build()).entity(androidVariant).build();
    }

    /**
     * List Android Variants for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return list of {@link AndroidVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllAndroidVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (application == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        return Response.ok(getVariants(application)).build();
    }

    /**
     * Update Android Variant
     *
     * @param pushApplicationId         id of {@link PushApplication}
     * @param androidID                 id of {@link AndroidVariant}
     * @param updatedAndroidApplication new info of {@link AndroidVariant}
     *
     * @return updated {@link AndroidVariant}
     *
     * @statuscode 200 The Android Variant updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested Android Variant resource does not exist
     */
    @PUT
    @Path("/{androidID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAndroidVariant(
            @PathParam("pushAppID") String pushApplicationId,
            @PathParam("androidID") String androidID,
            AndroidVariant updatedAndroidApplication) {

        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationId);

        if (application == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        // some validation
        var variant = variantService.findByVariantID(androidID);

        if (variant != null) {
            if (!(variant instanceof AndroidVariant)) {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
            }
            AndroidVariant androidVariant = (AndroidVariant) variant;
            try {
                updatedAndroidApplication.merge(androidVariant);
                validateModelClass(androidVariant);
            } catch (ConstraintViolationException cve) {
                logger.info("Unable to update Android Variant '{}'", androidVariant.getVariantID());
                logger.debug("Details: {}", cve);

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }


            logger.trace("Updating Android Variant '{}'", androidID);
            variantService.updateVariant(androidVariant);
            return Response.ok(androidVariant).build();
        }

        return Response.status(Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
    }

}
