package org.jboss.aerogear.unifiedpush.rest.registry.applications;
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

import org.jboss.aerogear.unifiedpush.api.ChromePackagedAppVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
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
import java.util.UUID;

@Stateless
@Path("/applications/{pushAppID}/chrome")
public class ChromePackagedAppEndpoint extends AbstractVariantEndpoint {

    // ===============================================================
    // =============== Mobile variant construct ======================
    // ===============     Chrome Packaged App  ======================
    // ===============================================================
    // new Chrome Packaged App
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerChromePackagedAppVariant(
            ChromePackagedAppVariant chromePackagedAppVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo,
            @Context HttpServletRequest request) {

        // find the root push app
        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, request.getUserPrincipal().getName());

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
        }

        // poor validation
        if (chromePackagedAppVariant.getClientSecret() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // manually set the ID:
        chromePackagedAppVariant.setVariantID(UUID.randomUUID().toString());
        // store the "developer:
        chromePackagedAppVariant.setDeveloper(request.getUserPrincipal().getName());

        // store the Chrome Packaged App variant:
        variantService.addVariant(chromePackagedAppVariant);
        pushAppService.addChromePackagedAppVariant(pushApp, chromePackagedAppVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(chromePackagedAppVariant.getVariantID())).build()).entity(chromePackagedAppVariant).build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllChromePackagedAppVariationsForPushApp(@Context HttpServletRequest request, @PathParam("pushAppID") String pushApplicationID) {
        return Response.ok(pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, request.getUserPrincipal().getName()).getChromePackagedAppVariants()).build();
    }

    // UPDATE
    @PUT
    @Path("/{chromeAppID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateChromePackagedAppVariation(
            @Context HttpServletRequest request,
            @PathParam("pushAppID") String id,
            @PathParam("chromeAppID") String chromeAppID,
            ChromePackagedAppVariant updatedChromePackagedApplication) {

        ChromePackagedAppVariant chromePackagedAppVariant = (ChromePackagedAppVariant) variantService.findByVariantIDForDeveloper(chromeAppID, request.getUserPrincipal().getName());
        if (chromePackagedAppVariant != null) {

            // poor validation
            //is different
            if (updatedChromePackagedApplication.getClientSecret() == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            // apply updated data:
            chromePackagedAppVariant.setClientId(updatedChromePackagedApplication.getClientId());
            chromePackagedAppVariant.setClientSecret(updatedChromePackagedApplication.getClientSecret());
            chromePackagedAppVariant.setRefreshToken(updatedChromePackagedApplication.getRefreshToken());
            chromePackagedAppVariant.setName(updatedChromePackagedApplication.getName());
            chromePackagedAppVariant.setDescription(updatedChromePackagedApplication.getDescription());
            variantService.updateVariant(chromePackagedAppVariant);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }
}
