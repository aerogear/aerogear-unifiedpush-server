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
package org.jboss.aerogear.connectivity.rest.registry.installations;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.connectivity.api.Variant;
import org.jboss.aerogear.connectivity.model.InstallationImpl;
import org.jboss.aerogear.connectivity.rest.security.util.HttpBasicHelper;
import org.jboss.aerogear.connectivity.service.ClientInstallationService;
import org.jboss.aerogear.connectivity.service.GenericVariantService;

@Stateless
@Path("/registry/device")
@TransactionAttribute
public class InstallationRegistrationEndpoint {
    @Inject
    private Logger logger;
    @Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private GenericVariantService genericVariantService;

    @OPTIONS
    @Path("{token}")
    public Response crossOriginForInstallations(
            @Context HttpHeaders headers,
            @PathParam("token") String token) {

        return appendPreflightResponseHeaders(headers, Response.ok()).build();
    }

    @OPTIONS
    public Response crossOriginForInstallations(@Context HttpHeaders headers) {

        return appendPreflightResponseHeaders(headers, Response.ok()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerInstallation(
            InstallationImpl entity,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return appendAllowOriginHeader(
                    Response.status(Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                    .entity("Unauthorized Request"),
                    request);
        }

        // Poor validation: We require the Token!
        if (entity.getDeviceToken() == null) {
            return appendAllowOriginHeader(Response.status(Status.BAD_REQUEST), request);
        }

        // look up all installations (with same token) for the given variant:
        InstallationImpl installation = 
                clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), entity.getDeviceToken()); 

        // new device/client ? 
        if (installation == null) {
            logger.finest("Performing client registration for: " + entity.getDeviceToken());
            // store the installation:
            entity = clientInstallationService.addInstallation(entity);
            // add installation to the matching variant
            genericVariantService.addInstallation(variant, entity);
        } else {
            // We only update the metadata, if the device is enabled: 
            if (installation.isEnabled()) {
                logger.info("Updating received metadata for Installation");
                // update the entity:
                entity = clientInstallationService.updateInstallation(installation, entity);
            }
        }

        return appendAllowOriginHeader(Response.ok(entity), request);
    }

    @DELETE
    @Path("{token}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unregisterInstallations(
            @PathParam("token") String token,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return appendAllowOriginHeader(
                    Response.status(Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                    .entity("Unauthorized Request"),
                    request);
        }

        // look up all installations (with same token) for the given variant:
        InstallationImpl installation = 
                clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), token);

        if (installation == null) {
            return appendAllowOriginHeader(Response.status(Status.NOT_FOUND), request);
        } else {
            logger.info("Deleting metadata Installation");
            // remove
            clientInstallationService.removeInstallation(installation);
        }

        return appendAllowOriginHeader(Response.noContent(), request);
    }

    private ResponseBuilder appendPreflightResponseHeaders(HttpHeaders headers, ResponseBuilder response) {
        // add response headers for the preflight request
        // required
        response.header("Access-Control-Allow-Origin", headers.getRequestHeader("Origin").get(0)) // return submitted origin
                .header("Access-Control-Allow-Methods", "POST, DELETE") // only POST/DELETE are allowed
                .header("Access-Control-Allow-Headers", "accept, origin, content-type, authorization") // explicit Headers!
                .header("Access-Control-Allow-Credentials", "true");

        return response;
    }

    private Response appendAllowOriginHeader(ResponseBuilder rb, HttpServletRequest request) {

        return rb.header("Access-Control-Allow-Origin", request.getHeader("Origin")) // return submitted origin
                .header("Access-Control-Allow-Credentials", "true")
                 .build();
    }

    /**
     * returns application if the masterSecret is valid for the request
     * PushApplication
     */
    private Variant loadVariantWhenAuthorized(
            HttpServletRequest request) {
        // extract the pushApplicationID and its secret from the HTTP Basic
        // header:
        String[] credentials = HttpBasicHelper
                .extractUsernameAndPasswordFromBasicHeader(request);
        String variantID = credentials[0];
        String secret = credentials[1];

        final Variant variant = genericVariantService.findByVariantID(variantID);
        if (variant != null && variant.getSecret().equals(secret)) {
            return variant;
        }

        // unauthorized...
        return null;
    }
}