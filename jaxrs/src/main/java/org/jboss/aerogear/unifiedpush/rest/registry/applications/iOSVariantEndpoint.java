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
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.event.iOSVariantUpdateEvent;
import org.jboss.aerogear.unifiedpush.rest.annotations.PATCH;
import org.jboss.aerogear.unifiedpush.rest.util.iOSApplicationUploadForm;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.enterprise.event.Event;
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

@Path("/applications/{pushAppID}/ios")
public class iOSVariantEndpoint extends AbstractVariantEndpoint {

    @Inject
    protected Event<iOSVariantUpdateEvent> variantUpdateEventEvent;

    @Inject
    public iOSVariantEndpoint(Validator validator, SearchManager searchManager) {
        super(validator, searchManager);
    }

    /**
     * Add iOS Variant
     *
     * @param form              new iOS Variant
     * @param pushApplicationID id of {@link PushApplication}
     * @param uriInfo           uri
     * @return                  created {@link iOSVariant}
     *
     * @statuscode 201 The iOS Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registeriOSVariant(
            @MultipartForm iOSApplicationUploadForm form,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {
        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
        }

        // some model validation on the uploaded form
        try {
            validateModelClass(form);
        } catch (ConstraintViolationException cve) {
            logger.trace("Unable to validate given form upload");

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());
            return builder.build();
        }

        // extract form values:
        iOSVariant iOSVariant = new iOSVariant();
        iOSVariant.setName(form.getName());
        iOSVariant.setDescription(form.getDescription());
        iOSVariant.setPassphrase(form.getPassphrase());
        iOSVariant.setCertificate(form.getCertificate());
        iOSVariant.setProduction(form.getProduction());

        // some model validation on the entity:
        try {
            validateModelClass(iOSVariant);
        } catch (ConstraintViolationException cve) {
            logger.trace("Unable to create iOS variant entity");

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        logger.trace("Register iOS variant with Push Application '{}'", pushApplicationID);
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
     * @return                  updated {@link iOSVariant}
     * @return                  list of {@link iOSVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAlliOSVariantsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, iOSVariant.class)).build();
    }

    /**
     * Update iOS Variant
     *
     * @param pushApplicationId id of {@link PushApplication}
     * @param iOSID             id of {@link iOSVariant}
     * @param updatediOSVariant updated version of {@link iOSVariant}
     * @return                  updated {@link iOSVariant}
     *
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
            iOSVariant updatediOSVariant) {

        iOSVariant iOSVariant = (iOSVariant)variantService.findByVariantID(iOSID);

        if (iOSVariant != null) {

            // apply update:
            iOSVariant.setName(updatediOSVariant.getName());
            iOSVariant.setDescription(updatediOSVariant.getDescription());
            iOSVariant.setProduction(updatediOSVariant.isProduction());
            logger.trace("Updating text details on iOS Variant '{}'", iOSID);

            variantService.updateVariant(iOSVariant);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    /**
     * Update iOS Variant
     *
     * @param pushApplicationId     id of {@link PushApplication}
     * @param iOSID                 id of {@link iOSVariant}
     * @param updatedForm           new info of {@link iOSVariant}
     *
     * @return                  updated {@link iOSVariant}
     *
     * @statuscode 200 The iOS Variant updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested iOS Variant resource does not exist
     */
    @PUT
    @Path("/{iOSID}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateiOSVariant(
            @MultipartForm iOSApplicationUploadForm updatedForm,
            @PathParam("pushAppID") String pushApplicationId,
            @PathParam("iOSID") String iOSID) {

        iOSVariant iOSVariant = (iOSVariant)variantService.findByVariantID(iOSID);
        if (iOSVariant != null) {

            // some model validation on the uploaded form
            try {
                validateModelClass(updatedForm);
            } catch (ConstraintViolationException cve) {
                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());
                return builder.build();
            }

            // apply update:
            iOSVariant.setName(updatedForm.getName());
            iOSVariant.setDescription(updatedForm.getDescription());
            iOSVariant.setPassphrase(updatedForm.getPassphrase());
            iOSVariant.setCertificate(updatedForm.getCertificate());
            iOSVariant.setProduction(updatedForm.getProduction());

            // some model validation on the entity:
            try {
                validateModelClass(iOSVariant);
            } catch (ConstraintViolationException cve) {
                logger.info("Unable to update iOS Variant '{}'", iOSVariant.getVariantID());
                logger.debug("Details: {}", cve);

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // update performed, we now need to invalidate existing connection w/ APNs:
            logger.trace("Updating iOS Variant '{}'", iOSVariant.getVariantID());
            variantUpdateEventEvent.fire(new iOSVariantUpdateEvent(iOSVariant));
            variantService.updateVariant(iOSVariant);
            return Response.ok(iOSVariant).build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    /**
     * Get the iOS Variant for the given ID
     *
     * @param variantId id of {@link Variant}
     * @return          requested {@link Variant}
     *
     * @statuscode 400 The requested Variant resource exists but it is not for iOS
     * @statuscode 404 The requested iOS Variant resource does not exist
     */
    @GET
    @Path("/{variantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findVariantById(@PathParam("variantId") String variantId) {
        return doFindVariantById(variantId, iOSVariant.class);
    }

    /**
     * Delete the iOS Variant
     *
     * @param variantId id of {@link Variant}
     *
     * @return no content or 404
     *
     * @statuscode 204 The Variant successfully deleted
     * @statuscode 400 The requested Variant resource exists but it is not for iOS
     * @statuscode 404 The requested iOS Variant resource does not exist
     */
    @DELETE
    @Path("/{variantId}")
    public Response deleteVariant(@PathParam("variantId") String variantId) {
        return this.doDeleteVariant(variantId, iOSVariant.class);
    }

    /**
     * Reset secret of the given iOS Variant
     *
     * @param variantId id of {@link Variant}
     * @return          {@link Variant} with new secret
     *
     * @statuscode 200 The secret of iOS Variant reset successfully
     * @statuscode 400 The requested Variant resource exists but it is not for iOS
     * @statuscode 404 The requested iOS Variant resource does not exist
     */
    @PUT
    @Path("/{variantId}/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetSecret(@PathParam("variantId") String variantId) {
        return doResetSecret(variantId, iOSVariant.class);
    }

}
