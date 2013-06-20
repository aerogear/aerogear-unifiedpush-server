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

package org.jboss.aerogear.connectivity.rest.registry.applications;

import org.jboss.aerogear.connectivity.cdi.interceptor.Secure;
import org.jboss.aerogear.connectivity.model.PushApplication;
import org.jboss.aerogear.connectivity.model.SimplePushVariant;
import org.jboss.aerogear.connectivity.service.PushApplicationService;
import org.jboss.aerogear.connectivity.service.SimplePushVariantService;
import org.jboss.aerogear.security.auth.LoggedUser;

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
@Path("/applications/{pushAppID}/simplePush")
@Secure("developer")
public class SimplePushVariantEndpoint {

    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private SimplePushVariantService simplePushVariantService;

    @Inject
    @LoggedUser
    private Instance<String> loginName;

    // ===============================================================
    // =============== Mobile variant construct ======================
    // ===============        SimplePush        ======================
    // ===============================================================

    // new SimplePush
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerSimplePushVariant(
            SimplePushVariant spv,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        // poor validation
        if (spv.getPushNetworkURL() == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        // manually set the ID:
        spv.setVariantID(UUID.randomUUID().toString());
        // store the "developer:
        spv.setDeveloper(loginName.get());

        // store the SimplePush variant:
        spv = simplePushVariantService.addSimplePushVariant(spv);
        // add iOS variant, and merge:
        pushAppService.addSimplePushVariant(pushApp, spv);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(spv.getVariantID())).build()).entity(spv).build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllSimplePushVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {

        return Response.ok(pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get()).getSimplePushApps()).build();
    }

    @GET
    @Path("/{simplePushID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findSimplePushVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("simplePushID") String simplePushID) {

        SimplePushVariant spv = simplePushVariantService.findByVariantIDForDeveloper(simplePushID, loginName.get());
        if (spv != null) {
            return Response.ok(spv).build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }

    // UPDATE
    @PUT
    @Path("/{simplePushID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSimplePushVariation(
            @PathParam("pushAppID") String id,
            @PathParam("simplePushID") String simplePushID,
            SimplePushVariant updatedSimplePushApplication) {

        SimplePushVariant spVariant = simplePushVariantService.findByVariantIDForDeveloper(simplePushID, loginName.get());
        if (spVariant != null) {

            // poor validation
            if (updatedSimplePushApplication.getPushNetworkURL() == null) {
                return Response.status(Status.BAD_REQUEST).build();
            }

            // apply updated data:
            spVariant.setName(updatedSimplePushApplication.getName());
            spVariant.setDescription(updatedSimplePushApplication.getDescription());
            spVariant.setPushNetworkURL(updatedSimplePushApplication.getPushNetworkURL());
            simplePushVariantService.updateSimplePushVariant(spVariant);
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }

    // DELETE
    @DELETE
    @Path("/{simplePushID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteSimplePushVariation(@PathParam("pushAppID") String pushApplicationID, @PathParam("simplePushID") String simplePushID) {

        SimplePushVariant spVariant = simplePushVariantService.findByVariantIDForDeveloper(simplePushID, loginName.get());
        if (spVariant != null) {
            PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());
            pushApp.getSimplePushApps().remove(spVariant);
            simplePushVariantService.removeSimplePushVariant(spVariant);
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).build();
    }
}
