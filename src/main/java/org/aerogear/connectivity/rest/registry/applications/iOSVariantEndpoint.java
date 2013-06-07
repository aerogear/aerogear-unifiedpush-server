/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aerogear.connectivity.rest.registry.applications;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.model.iOSVariant;
import org.aerogear.connectivity.rest.util.iOSApplicationUploadForm;
import org.aerogear.connectivity.service.PushApplicationService;
import org.aerogear.connectivity.service.iOSVariantService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

@Stateless
@TransactionAttribute
@Path("/applications/{pushAppID}/iOS")
public class iOSVariantEndpoint {
    
    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private iOSVariantService iOSVariantService;
   
    
    // ===============================================================
    // =============== Mobile variant construct ======================
    // ===============           iOS            ======================
    // ===============================================================
    // new iOS
    @POST
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public Response registeriOSVariant(
            @MultipartForm iOSApplicationUploadForm form, 
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = pushAppService.findByPushApplicationID(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        // poor validation
        if (form.getCertificate() == null || form.getPassphrase() == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        // extract form values:
        iOSVariant iOSVariation = new iOSVariant();
        iOSVariation.setName(form.getName());
        iOSVariation.setDescription(form.getDescription());
        iOSVariation.setPassphrase(form.getPassphrase());
        iOSVariation.setCertificate(form.getCertificate());
        
        // manually set the ID:
        iOSVariation.setVariantID(UUID.randomUUID().toString());

        // store the iOS variant:
        iOSVariation = iOSVariantService.addiOSVariant(iOSVariation);

        // add iOS variant, and merge:
        pushAppService.addiOSVariant(pushApp, iOSVariation);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(iOSVariation.getVariantID())).build()).entity(iOSVariation).build();
   }
    // READ
    @GET
    @Produces("application/json")
    public Response listAlliOSVariationsForPushApp(@PathParam("pushAppID") String pushAppID)  {
        return Response.ok(pushAppService.findByPushApplicationID(pushAppID)).build();
    }

    @GET
    @Path("/{iOSID}")
    @Produces("application/json")
    public Response findiOSVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("iOSID") String iOSID) {
        iOSVariant iOSvariant = iOSVariantService.findByVariantID(iOSID);
        
        if (iOSvariant != null) {
            return Response.ok(iOSvariant).build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    // UPDATE
    @PUT
    @Path("/{iOSID}")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public Response updateiOSVariant(
            @MultipartForm iOSApplicationUploadForm updatedForm, 
            @PathParam("pushAppID") String pushApplicationId,
            @PathParam("iOSID") String iOSID) {

        iOSVariant iOSVariation = iOSVariantService.findByVariantID(iOSID);
        if (iOSVariation != null) {

            // poor validation
            if (updatedForm.getCertificate() == null || updatedForm.getPassphrase() == null) {
                return Response.status(Status.BAD_REQUEST).build();
            }

            // apply update:
            iOSVariation.setName(updatedForm.getName());
            iOSVariation.setDescription(updatedForm.getDescription());
            iOSVariation.setPassphrase(updatedForm.getPassphrase());
            iOSVariation.setCertificate(updatedForm.getCertificate());

            iOSVariantService.updateiOSVariant(iOSVariation);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }

    // DELETE
    @DELETE
    @Path("/{iOSID}")
    @Consumes("application/json")
    public Response deleteiOSVariation(@PathParam("pushAppID") String id, @PathParam("iOSID") String iOSID) {
        iOSVariant iOSVariation = iOSVariantService.findByVariantID(iOSID);
        
        if (iOSVariation != null) {
            iOSVariantService.removeiOSVariant(iOSVariation);
            return Response.noContent().build();
        }
        return Response.status(Status.NOT_FOUND).build();
    }
  
}