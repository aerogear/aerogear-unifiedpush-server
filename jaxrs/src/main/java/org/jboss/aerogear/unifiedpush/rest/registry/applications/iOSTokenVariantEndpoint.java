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
import org.jboss.aerogear.unifiedpush.api.iOSTokenVariant;
import org.jboss.aerogear.unifiedpush.message.jms.APNSClientProducer;
import org.jboss.aerogear.unifiedpush.rest.annotations.DisabledByEnvironment;
import org.jboss.aerogear.unifiedpush.rest.annotations.PATCH;
import org.jboss.aerogear.unifiedpush.rest.util.error.ErrorBuilder;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

@Path("/applications/{pushAppID}/{ignore:iostoken|ios_token}")
@DisabledByEnvironment({"ios_token", "iostoken"})
public class iOSTokenVariantEndpoint extends AbstractVariantEndpoint<iOSTokenVariant> {

    @Inject
    protected APNSClientProducer producer;

    // required for RESTEasy
    public iOSTokenVariantEndpoint() {
        super(iOSTokenVariant.class);
    }

    // required for tests
    iOSTokenVariantEndpoint(Validator validator, SearchManager searchManager) {
        super(validator, searchManager, iOSTokenVariant.class);
    }

    /**
     * Add iOS Variant
     *
     * @param iOSVariant        new iOS Token Variant
     * @param pushApplicationID id of {@link PushApplication}
     * @param uriInfo           uri
     * @return created {@link iOSTokenVariant}
     * @statuscode 201 The iOS Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registeriOSVariant(
            iOSTokenVariant iOSVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {
        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        // some model validation on the entity:
        try {
            validateModelClass(iOSVariant);
        } catch (ConstraintViolationException cve) {
            logger.trace("Unable to create iOS variant entity");

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        logger.trace("Register iOS token variant with Push Application '{}'", pushApplicationID);
        // store the iOS variant:
        variantService.addVariant(iOSVariant);
        // add iOS variant, and merge:
        pushAppService.addVariant(pushApp, iOSVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(iOSVariant.getVariantID())).build()).entity(iOSVariant).build();
    }

    /**
     * List iOS Variants for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return list of {@link iOSTokenVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAlliOSVariantsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (application == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        return Response.ok(getVariants(application)).build();
    }

    /**
     * Update iOS Variant
     *
     * @param pushApplicationId id of {@link PushApplication}
     * @param iOSID             id of {@link iOSTokenVariant}
     * @param updatediOSVariant updated version of {@link iOSTokenVariant}
     * @return updated {@link iOSTokenVariant}
     * @statuscode 204 The iOS Variant updated successfully
     * @statuscode 404 The requested Variant resource does not exist
     */
    @PATCH
    @Path("/{iOSID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateiOSVariant(
            @PathParam("pushAppID") String pushApplicationId,
            @PathParam("iOSID") String iOSID,
            iOSTokenVariant updatediOSVariant) {

        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationId);

        if (application == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        iOSTokenVariant iOSTokenVariant = (iOSTokenVariant) variantService.findByVariantID(iOSID);

        if (iOSTokenVariant != null) {

            // apply update:

            // merge the values and validate the new model
            try {
                updatediOSVariant.merge(iOSTokenVariant);
                validateModelClass(iOSTokenVariant);
            } catch (ConstraintViolationException cve) {
                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());
                return builder.build();
            }


            logger.trace("Updating text details on iOS Variant '{}'", iOSID);

            variantService.updateVariant(iOSTokenVariant);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
    }

    /**
     * Update iOS Variant
     *
     * @param pushApplicationId id of {@link PushApplication}
     * @param iOSID             id of {@link iOSTokenVariant}
     * @param updatediOSVariant new info of {@link iOSTokenVariant}
     * @return updated {@link iOSTokenVariant}
     * @statuscode 200 The iOS Variant updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested iOS Variant resource does not exist
     */
    @PUT
    @Path("/{iOSID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateiOSVariant(
            iOSTokenVariant updatediOSVariant,
            @PathParam("pushAppID") String pushApplicationId,
            @PathParam("iOSID") String iOSID) {

        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationId);

        if (application == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }


        var variant = variantService.findByVariantID(iOSID);

        if (variant != null) {
            if (!(variant instanceof iOSTokenVariant)) {
                return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
            }
            iOSTokenVariant iOSTokenVariant = (iOSTokenVariant) variant;
            // merge the values and validate the new model
            try {
                updatediOSVariant.merge(iOSTokenVariant);
                validateModelClass(iOSTokenVariant);
            } catch (ConstraintViolationException cve) {
                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());
                return builder.build();
            }

            // update performed, we now need to invalidate existing connection w/ APNs:
            logger.trace("Updating iOS Variant '{}'", iOSTokenVariant.getVariantID());
            variantService.updateVariant(iOSTokenVariant);
            producer.changeAPNClient(iOSTokenVariant);

            return Response.ok(iOSTokenVariant).build();
        }
        return Response.status(Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
    }
}
