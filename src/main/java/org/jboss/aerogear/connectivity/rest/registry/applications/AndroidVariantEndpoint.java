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
package org.jboss.aerogear.connectivity.rest.registry.applications;

import org.jboss.aerogear.connectivity.model.AndroidVariant;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.service.AndroidVariantService;
import org.jboss.aerogear.connectivity.service.PushApplicationService;
import org.jboss.aerogear.security.auth.LoggedUser;
import org.jboss.aerogear.security.authz.Secure;

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

@Stateless
@TransactionAttribute
@Path("/applications/{pushAppID}/android")
@Secure("developer")
public class AndroidVariantEndpoint {

    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private AndroidVariantService androidVariantService;

    @Inject
    @LoggedUser
    private Instance<String> loginName;

    // ===============================================================
    // =============== Mobile variant construct ======================
    // ===============         Android          ======================
    // ===============================================================
    // new Android
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerAndroidVariant(
            AndroidVariant androidVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplication").build();
        }

        // poor validation
        if (androidVariant.getGoogleKey() == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        // manually set the ID:
        androidVariant.setVariantID(UUID.randomUUID().toString());
        // store the "developer:
        androidVariant.setDeveloper(loginName.get());

        // store the Android variant:
        androidVariant = androidVariantService.addAndroidVariant(androidVariant);
        // add iOS variant, and merge:
        pushAppService.addAndroidVariant(pushApp, androidVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(androidVariant.getVariantID())).build()).entity(androidVariant).build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllAndroidVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        return Response.ok(pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get()).getAndroidApps()).build();
    }

    @GET
    @Path("/{androidID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findAndroidVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("androidID") String androidID) {

        AndroidVariant androidVariant = androidVariantService.findByVariantIDForDeveloper(androidID, loginName.get());

        if (androidVariant != null) {
            return Response.ok(androidVariant).build();
        }
        return Response.status(Status.NOT_FOUND).entity("Could not find requested MobileVariant").build();
    }

    // UPDATE
    @PUT
    @Path("/{androidID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAndroidVariation(
            @PathParam("pushAppID") String id,
            @PathParam("androidID") String androidID,
            AndroidVariant updatedAndroidApplication) {

        AndroidVariant androidVariant = androidVariantService.findByVariantIDForDeveloper(androidID, loginName.get());
        if (androidVariant != null) {

            // poor validation
            if (updatedAndroidApplication.getGoogleKey() == null) {
                return Response.status(Status.BAD_REQUEST).build();
            }

            // apply updated data:
            androidVariant.setGoogleKey(updatedAndroidApplication.getGoogleKey());
            androidVariant.setName(updatedAndroidApplication.getName());
            androidVariant.setDescription(updatedAndroidApplication.getDescription());
            androidVariantService.updateAndroidVariant(androidVariant);
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested MobileVariant").build();
    }

    // DELETE
    @DELETE
    @Path("/{androidID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteAndroidVariation(@PathParam("pushAppID") String pushApplicationID, @PathParam("androidID") String androidID) {

        AndroidVariant androidVariant = androidVariantService.findByVariantIDForDeveloper(androidID, loginName.get());

        if (androidVariant != null) {
            PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());
            pushApp.getAndroidApps().remove(androidVariant);
            androidVariantService.removeAndroidVariant(androidVariant);
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested MobileVariant").build();
    }
}
