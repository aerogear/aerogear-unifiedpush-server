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

import org.jboss.aerogear.crypto.util.PKCS12;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.rest.annotations.PATCH;
import org.jboss.aerogear.unifiedpush.rest.util.iOSApplicationUploadForm;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.ejb.Stateless;
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

@Stateless
@Path("/applications/{pushAppID}/ios")
public class iOSVariantEndpoint extends AbstractVariantEndpoint {

    // new iOS
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

        // uploaded certificate/passphrase pair OK (do they match)?
        if (!validateCertificateAndPassphrase(form)) {
            // nope, keep 400 response empty to not leak details about cert/passphrase
            return Response.status(Status.BAD_REQUEST).build();
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

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // store the iOS variant:
        variantService.addVariant(iOSVariant);

        // add iOS variant, and merge:
        pushAppService.addVariant(pushApp, iOSVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(iOSVariant.getVariantID())).build()).entity(iOSVariant).build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAlliOSVariantsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, iOSVariant.class)).build();
    }

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

            variantService.updateVariant(iOSVariant);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    // UPDATE
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

            // uploaded certificate/passphrase pair OK (do they match)?
            if (!validateCertificateAndPassphrase(updatedForm)) {
                // nope, keep 400 response empty to not leak details about cert/passphrase
                return Response.status(Status.BAD_REQUEST).build();
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

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            variantService.updateVariant(iOSVariant);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    /**
     * Helper to validate if we got a certificate/passphrase pair AND (if present)
     * if that pair is also valid, and does not contain any bogus content.
     *
     * @return true if valid, otherwise false
     */
    private boolean validateCertificateAndPassphrase(iOSApplicationUploadForm form) {

        // got certificate/passphrase, with content that makes sense ?
        try {
            PKCS12.validate(form.getCertificate(), form.getPassphrase());

            // ok we are good:
            return true;
        } catch (Exception e) {
            logger.severe("Could not validate the given certificate and passphrase pair");
            return false;
        }
    }
}
