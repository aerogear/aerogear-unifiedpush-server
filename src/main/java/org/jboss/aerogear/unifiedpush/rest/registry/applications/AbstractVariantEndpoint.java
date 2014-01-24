/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
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
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.security.auth.LoggedUser;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.model.PushApplication;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.UserService;
import org.jboss.aerogear.unifiedpush.users.UserRoles;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Abstract base class for all the concrete variant endpoints. Shares common
 * functionality.
 */
public abstract class AbstractVariantEndpoint extends AbstractBaseEndpoint {

    @Inject
    protected PushApplicationService pushAppService;

    @Inject
    protected GenericVariantService variantService;

    @Inject
    protected Logger logger;

    // Secret Reset
    @PUT
    @Path("/{variantId}/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response resetSecret(@PathParam("variantId") String variantId) {

        Variant variant = getVariantById(variantId);

        if (variant != null) {
            logger.finest(String.format("Resetting secret: %s", variant.getClass().getSimpleName()));

            // generate the new 'secret' and apply it:
            String newSecret = UUID.randomUUID().toString();
            variant.setSecret(newSecret);
            variantService.updateVariant(variant);

            return Response.ok(variant).build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    @GET
    @Path("/{variantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findVariantById(@PathParam("variantId") String variantId) {

        Variant variant = getVariantById(variantId);

        if (variant != null) {
            logger.finest(String.format("Requested: %s", variant));

            return Response.ok(variant).build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    // DELETE
    @DELETE
    @Path("/{variantId}")
    public Response deleteVariant(@PathParam("variantId") String variantId) {

        Variant variant = getVariantById(variantId);

        if (variant != null) {
            logger.finest(String.format("Deleting: %s", variant.getClass().getSimpleName()));

            variantService.removeVariant(variant);
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }

    protected Variant getVariantById(String variantId) {
        if(isUserAdminOrViewer()) {
            return variantService.findByVariantID(variantId);
        }
        else {
            return variantService.findByVariantIDForDeveloper(variantId, userService.getLoginName());
        }
    }

    protected PushApplication getPushApplicationById(String pushApplicationID){
        if(isUserAdminOrViewer()) {
            return pushAppService.findByPushApplicationID(pushApplicationID);
        }
        else {
            return pushAppService.findByPushApplicationIDForDeveloper(pushApplicationID, userService.getLoginName());
        }
    }

}
