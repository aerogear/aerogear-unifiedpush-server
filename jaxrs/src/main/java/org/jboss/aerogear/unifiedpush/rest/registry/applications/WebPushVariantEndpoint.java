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

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.jboss.aerogear.unifiedpush.rest.annotations.DisabledByEnvironment;
import org.jboss.aerogear.unifiedpush.rest.util.error.ErrorBuilder;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/applications/{pushAppID}/{ignore:webpush|web_push}")
@DisabledByEnvironment({"webpush","web_push"})
public class WebPushVariantEndpoint extends AbstractVariantEndpoint<WebPushVariant> {

    public WebPushVariantEndpoint() {
        super(WebPushVariant.class);
    }

    // required for tests
    WebPushVariantEndpoint(Validator validator, SearchManager searchManager) {
        super(validator, searchManager, WebPushVariant.class);
    }

    /**
     * Add Webpush Variant
     *
     * @param webPushVariant    new {@link WebPushVariant}
     * @param pushApplicationID id of {@link PushApplication}
     * @param uriInfo           the uri
     * @return created {@link WebPushVariant}
     *
     * @statuscode 201 The WebPush Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerWebPushVariant(
            WebPushVariant webPushVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        // some validation
        try {
            validateModelClass(webPushVariant);
        } catch (ConstraintViolationException cve) {
            logger.trace("Unable to create WebPush variant");
            // Build and return the 400 (Bad Request) response
            Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        logger.trace("Register WebPush variant with Push Application '{}'", pushApplicationID);
        // store the webPushVariant :
        variantService.addVariant(webPushVariant);
        // add webPushVariant, and merge:
        pushAppService.addVariant(pushApp, webPushVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(webPushVariant.getVariantID())).build()).entity(webPushVariant).build();
    }


    /**
     * List WebPush Variants for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return list of {@link WebPushVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllWebPushVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (application == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        return Response.ok(getVariants(application)).build();
    }

    @PUT
    @Path("/{webPushID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateWebPushVariant(@PathParam("pushAppID") String pushApplicationID,
                                         @PathParam("webPushID") String webPushID, WebPushVariant updatedVariant) {

        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (application == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        var variant = variantService.findByVariantID(webPushID);

        if (variant != null) {
            if (!(variant instanceof WebPushVariant)) {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
            }
            WebPushVariant webPushVariant = (WebPushVariant) variant;
            // some validation
            try {
                updatedVariant.merge(webPushVariant);
                validateModelClass(webPushVariant);
            } catch (ConstraintViolationException cve) {
                logger.info("Unable to update WebPush Variant '{}'", webPushVariant.getVariantID());
                logger.debug("Details: {}", cve);

                // Build and return the 400 (Bad Request) response
                Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            logger.trace("Updating WebPush Variant '{}'", webPushID);
            variantService.updateVariant(webPushVariant);
            return Response.ok(webPushVariant).build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
    }
}
