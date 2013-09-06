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

import org.jboss.aerogear.unifiedpush.annotations.PATCH;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.iOSVariant;
import org.jboss.aerogear.unifiedpush.rest.util.iOSApplicationUploadForm;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.iOSVariantService;
import org.jboss.aerogear.security.auth.LoggedUser;
import org.jboss.aerogear.security.authz.Secure;
import org.jboss.aerogear.security.util.PKCS12Util;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute
@Path("/applications/{pushAppID}/iOS")
@Secure( { "developer", "admin" })
public class iOSVariantEndpoint {

    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private iOSVariantService iOSVariantService;

    @Inject
    @LoggedUser
    private Instance<String> loginName;

    @Inject
    private Logger logger;

    // ===============================================================
    // =============== Mobile variant construct ======================
    // ===============           iOS            ======================
    // ===============================================================
    // new iOS
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registeriOSVariant(
            @MultipartForm iOSApplicationUploadForm form,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {
        // find the root push app
        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplication").build();
        }

        // certificate/passphrase pair OK ?
        if (!validateCertificateAndPassphrase(form)) {
            // nope...
            return Response.status(Status.BAD_REQUEST).build();
        }

        // extract form values:
        iOSVariant iOSVariation = new iOSVariant();
        iOSVariation.setName(form.getName());
        iOSVariation.setDescription(form.getDescription());
        iOSVariation.setPassphrase(form.getPassphrase());
        iOSVariation.setCertificate(form.getCertificate());
        iOSVariation.setProduction(form.getProduction());

        // manually set the ID:
        iOSVariation.setVariantID(UUID.randomUUID().toString());
        // store the "developer:
        iOSVariation.setDeveloper(loginName.get());
        // store the iOS variant:
        iOSVariation = iOSVariantService.addiOSVariant(iOSVariation);

        // add iOS variant, and merge:
        pushAppService.addiOSVariant(pushApp, iOSVariation);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(iOSVariation.getVariantID())).build()).entity(iOSVariation).build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAlliOSVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {

        return Response.ok(pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get()).getIOSVariants()).build();
    }

    @GET
    @Path("/{iOSID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findiOSVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("iOSID") String iOSID) {

        iOSVariant iOSvariant = iOSVariantService.findByVariantIDForDeveloper(iOSID, loginName.get());

        if (iOSvariant != null) {
            return Response.ok(iOSvariant).build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    @PATCH
    @Path("/{iOSID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateiOSVariant(
            @PathParam("pushAppID") String pushApplicationId,
            @PathParam("iOSID") String iOSID,
            iOSVariant updatediOSVariant) {

        iOSVariant iOSVariation = iOSVariantService.findByVariantIDForDeveloper(iOSID, loginName.get());

        if (iOSVariation != null) {

            // apply update:
            iOSVariation.setName(updatediOSVariant.getName());
            iOSVariation.setDescription(updatediOSVariant.getDescription());

            iOSVariantService.updateiOSVariant(iOSVariation);
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

        iOSVariant iOSVariation = iOSVariantService.findByVariantIDForDeveloper(iOSID, loginName.get());
        if (iOSVariation != null) {

            // certificate/passphrase pair OK ?
            if (!validateCertificateAndPassphrase(updatedForm)) {
                // nope...
                return Response.status(Status.BAD_REQUEST).build();
            }

            // apply update:
            iOSVariation.setName(updatedForm.getName());
            iOSVariation.setDescription(updatedForm.getDescription());
            iOSVariation.setPassphrase(updatedForm.getPassphrase());
            iOSVariation.setCertificate(updatedForm.getCertificate());
            iOSVariation.setProduction(updatedForm.getProduction());

            iOSVariantService.updateiOSVariant(iOSVariation);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    // DELETE
    @DELETE
    @Path("/{iOSID}")
    public Response deleteiOSVariation(@PathParam("pushAppID") String pushApplicationID, @PathParam("iOSID") String iOSID) {

        iOSVariant iOSVariation = iOSVariantService.findByVariantIDForDeveloper(iOSID, loginName.get());

        if (iOSVariation != null) {
            iOSVariantService.removeiOSVariant(iOSVariation);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    /** 
     * Helper to validate if we got a certificate/passphrase pair AND (if present)
     * if that pair is also valid, and does not contain any bogus content.
     * 
     * 
     *  @return true if valid, otherwise false
     */
    private boolean validateCertificateAndPassphrase(iOSApplicationUploadForm form) {

        // got certificate/passphrase, with content that makes sense ?
        try {
            PKCS12Util.validate(form.getCertificate(), form.getPassphrase());

            // ok we are good:
            return true;
        } catch (Exception e) {
            logger.severe("Could not validate the given certificate and passphrase pair");
            return false;
        }
    }
}
