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

import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
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
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;

@Path("/applications/{pushAppID}/webPush")
public class WebPushVariantEndpoint extends AbstractVariantEndpoint<WebPushVariant> {

    // required for RESTEasy
    public WebPushVariantEndpoint() {
        super(WebPushVariant.class);
    }

    // required for tests
    WebPushVariantEndpoint(Validator validator, SearchManager searchManager) {
        super(validator, searchManager, WebPushVariant.class);
    }

    /**
     * Add WebPush Variant
     *
     * @param webPushVariant new {@link WebPushVariant}
     * @param pushApplicationID id of {@link PushApplication}
     * @return created {@link WebPushVariant}
     *
     * @statuscode 201 The WebPush Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)    
    public Response registerWebPushVariant(
            WebPushVariant webPushVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Could not find requested PushApplicationEntity")
                    .build();
        }

        // some validation
        try {
            validateModelClass(webPushVariant);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // store the WebPush variant:
        variantService.addVariant(webPushVariant);
        // add WebPush variant, and merge:
        pushAppService.addVariant(pushApp, webPushVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(webPushVariant.getVariantID()).build())
                .entity(webPushVariant)
                .build();
    }

    /**
     * List WebPush Variants for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return list of {@link WebPushVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)    
    public Response listAllWebPushVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, WebPushVariant.class)).build();
    }

    /**
     * Update WebPush Variant
     *
     * @param id id of {@link PushApplication}
     * @param webPushID id of {@link WebPushVariant}
     * @param updatedWebPushVariant new info of {@link WebPushVariant}
     *
     * @statuscode 200 The WebPush Variant updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested WebPush Variant resource does not exist
     */
    @PUT
    @Path("/{webPushID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)    
    public Response updateWebPushVariation(
            @PathParam("pushAppID") String id,
            @PathParam("webPushID") String webPushID,
            WebPushVariant updatedWebPushVariant) {

        WebPushVariant webPushVariant = (WebPushVariant) variantService.findByVariantID(webPushID);
        if (webPushVariant != null) {

            // some validation
            try {
                validateModelClass(updatedWebPushVariant);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // apply updated data:
            webPushVariant.setName(updatedWebPushVariant.getName());
            webPushVariant.setDescription(updatedWebPushVariant.getDescription());
            // update FCM info:
            webPushVariant.setFcmServerKey(updatedWebPushVariant.getFcmServerKey());
            webPushVariant.setFcmSenderID(updatedWebPushVariant.getFcmSenderID());

            variantService.updateVariant(webPushVariant);
            return Response.ok(webPushVariant).build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }
}
