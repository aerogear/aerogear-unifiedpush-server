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

import org.jboss.aerogear.security.auth.LoggedUser;
import org.jboss.aerogear.security.authz.Secure;
import org.jboss.aerogear.unifiedpush.model.ChromePackagedAppVariant;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.service.ChromePackagedAppVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

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
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

@Stateless
@TransactionAttribute
@Path("/applications/{pushAppID}/chrome")
@Secure({ "developer", "admin" })
public class ChromePackagedAppEndpoint {

    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private ChromePackagedAppVariantService chromePackagedAppVariantService;

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
    public Response registerChromePackagedAppVariant(
            ChromePackagedAppVariant chromePackagedAppVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested PushApplication").build();
        }

        // poor validation
        if (chromePackagedAppVariant.getClientSecret() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        // manually set the ID:
        chromePackagedAppVariant.setVariantID(UUID.randomUUID().toString());
        // store the "developer:
        chromePackagedAppVariant.setDeveloper(loginName.get());

        // store the Android variant:
        chromePackagedAppVariant = chromePackagedAppVariantService.addChromePackagedApp(chromePackagedAppVariant);
        // add iOS variant, and merge:
        pushAppService.addChromePackagedAppVariant(pushApp, chromePackagedAppVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(chromePackagedAppVariant.getVariantID())).build()).entity(chromePackagedAppVariant).build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllChromePackagedAppVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        return Response.ok(pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get()).getChromePackagedAppVariants()).build();
    }

    @GET
    @Path("/{chromeAppID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findChromePackagedAppVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("chromeAppID") String chromeAppID) {

        ChromePackagedAppVariant chromePackagedAppVariant = chromePackagedAppVariantService.findByVariantIDForDeveloper(chromeAppID, loginName.get());

        if (chromePackagedAppVariant != null) {
            return Response.ok(chromePackagedAppVariant).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    // UPDATE
    @PUT
    @Path("/{chromeAppID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateChromePackagedAppVariation(
            @PathParam("pushAppID") String id,
            @PathParam("chromeAppID") String chromeAppID,
            ChromePackagedAppVariant updatedChromePackagedApplication) {

        ChromePackagedAppVariant chromePackagedAppVariant = chromePackagedAppVariantService.findByVariantIDForDeveloper(chromeAppID, loginName.get());
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
            chromePackagedAppVariantService.updateChromePackagedApp(chromePackagedAppVariant);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    // DELETE
    @DELETE
    @Path("/{chromeAppID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteChromePackagedAppVariation(@PathParam("pushAppID") String pushApplicationID, @PathParam("chromeAppID") String chromeAppID) {

        ChromePackagedAppVariant chromePackagedAppVariant = chromePackagedAppVariantService.findByVariantIDForDeveloper(chromeAppID, loginName.get());

        if (chromePackagedAppVariant != null) {
            chromePackagedAppVariantService.removeChromePackagedApp(chromePackagedAppVariant);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

}
