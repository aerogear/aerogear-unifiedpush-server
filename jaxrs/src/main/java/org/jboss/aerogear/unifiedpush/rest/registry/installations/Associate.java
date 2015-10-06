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
package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;

import com.qmino.miredot.annotations.ReturnType;

@Path("/associate")
public class Associate extends AbstractBaseEndpoint {
 
    @Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private GenericVariantService genericVariantService;
    
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.Installation")
    public Response associate(@Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return create401Response(request);
        }
        
        request.getHeader("deviceToken");
        String deviceToken = null;
		Installation installation = clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), deviceToken);
        if (installation == null) {
            return createNotAssociateResponse(request);
        }

        // Associate the device - find the matching application and update the device to the right application 
        clientInstallationService.associateInstallation(installation);
        Variant newVariant = installation.getVariant();
 
        return appendAllowOriginHeader(Response.ok(newVariant), request);
    }
    
    private Response appendAllowOriginHeader(ResponseBuilder rb, HttpServletRequest request) {

        return rb.header("Access-Control-Allow-Origin", request.getHeader("Origin")) // return submitted origin
                .header("Access-Control-Allow-Credentials", "true")
                 .build();
    }

    private Response create401Response(final HttpServletRequest request) {
        return appendAllowOriginHeader(
                Response.status(Status.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                        .entity("Unauthorized Request"),
                request);
    }
    
    private Response createNotAssociateResponse(final HttpServletRequest request) {
        return appendAllowOriginHeader(
                Response.ok(ASSOCIATION_STATUS.NOT_ASSOCIATED)
                .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\""),
                request);
    }
    
    private enum ASSOCIATION_STATUS {
    	ASSOCIATED,
    	NOT_ASSOCIATED;
    }
    
    /**
     * returns application if the masterSecret is valid for the request
     * PushApplicationEntity
     */
    private Variant loadVariantWhenAuthorized(
            HttpServletRequest request) {
        // extract the pushApplicationID and its secret from the HTTP Basic
        // header:
        String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
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
