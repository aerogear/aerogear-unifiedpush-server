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

import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.model.SimplePushVariant;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.SimplePushVariantService;
import org.jboss.aerogear.security.auth.LoggedUser;
import org.jboss.aerogear.security.authz.Secure;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

@Stateless
@TransactionAttribute
@Path("/applications/{pushAppID}/simplePush")
@Secure( { "developer", "admin" })
public class SimplePushVariantEndpoint extends AbstractBaseEndpoint {

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
            SimplePushVariant simplePushVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get());

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplication").build();
        }

        // some validation
        try {
            validateModelClass(simplePushVariant);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }


        // manually set the ID:
        simplePushVariant.setVariantID(UUID.randomUUID().toString());
        // store the "developer:
        simplePushVariant.setDeveloper(loginName.get());

        // store the SimplePush variant:
        simplePushVariant = simplePushVariantService.addSimplePushVariant(simplePushVariant);
        // add iOS variant, and merge:
        pushAppService.addSimplePushVariant(pushApp, simplePushVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(simplePushVariant.getVariantID())).build()).entity(simplePushVariant).build();
    }

    // READ
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllSimplePushVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {

        return Response.ok(pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, loginName.get()).getSimplePushVariants()).build();
    }

    @GET
    @Path("/{simplePushID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findSimplePushVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("simplePushID") String simplePushID) {

        SimplePushVariant spv = simplePushVariantService.findByVariantIDForDeveloper(simplePushID, loginName.get());
        if (spv != null) {
            return Response.ok(spv).build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
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

            // some validation
            try {
                validateModelClass(updatedSimplePushApplication);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // apply updated data:
            spVariant.setName(updatedSimplePushApplication.getName());
            spVariant.setDescription(updatedSimplePushApplication.getDescription());
            simplePushVariantService.updateSimplePushVariant(spVariant);
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    // DELETE
    @DELETE
    @Path("/{simplePushID}")
    public Response deleteSimplePushVariation(@PathParam("pushAppID") String pushApplicationID, @PathParam("simplePushID") String simplePushID) {

        SimplePushVariant spVariant = simplePushVariantService.findByVariantIDForDeveloper(simplePushID, loginName.get());
        if (spVariant != null) {
            simplePushVariantService.removeSimplePushVariant(spVariant);
            return Response.noContent().build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }
}
