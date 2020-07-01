/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.error.ErrorBuilder;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;

import javax.inject.Inject;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Abstract base class for all the concrete variant endpoints. Shares common
 * functionality.
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public abstract class AbstractVariantEndpoint<T extends Variant> extends AbstractBaseEndpoint {

    private final Class<T> type;

    @Inject
    protected PushApplicationService pushAppService;

    @Inject
    protected GenericVariantService variantService;

    AbstractVariantEndpoint(Class<T> type) {
        this.type = type;
    }

    // required for tests
    AbstractVariantEndpoint(Validator validator, SearchManager searchManager, Class<T> type) {
        super(validator, searchManager);
        this.type = type;
    }

    /**
     * Secret Reset
     *
     * @param variantId id of {@link Variant}
     * @return {@link Variant} with new secret
     * @statuscode 200 The secret of Variant reset successfully
     * @statuscode 404 The requested Variant resource exists but it is not of the given type
     * @statuscode 404 The requested Variant resource does not exist
     */
    @PUT
    @Path("/{variantId}/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetSecret(@PathParam("pushAppID") String pushApplicationID,
                                @PathParam("variantId") String variantId) {
        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        Variant variant = variantService.findByVariantID(variantId);

        if (variant == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
        }

        if (!type.isInstance(variant)) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
        }

        logger.trace("Resetting secret for: {}", variant.getName());

        // generate the new 'secret' and apply it:
        String newSecret = UUID.randomUUID().toString();
        variant.setSecret(newSecret);
        variantService.updateVariant(variant);

        return Response.ok(variant).build();

    }

    /**
     * Get Variant
     *
     * @param variantId id of {@link Variant}
     * @return requested {@link Variant}
     * @statuscode 404 The requested Variant resource exists but it is not of the given type
     * @statuscode 404 The requested Variant resource does not exist
     */
    @GET
    @Path("/{variantId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findVariantById(@PathParam("pushAppID") String pushApplicationID,
                                    @PathParam("variantId") String variantId) {

        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        Variant variant = variantService.findByVariantID(variantId);

        if (variant == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
        }

        if (!type.isInstance(variant)) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
        }

        return Response.ok(variant).build();
    }

    /**
     * Delete Variant
     *
     * @param variantId id of {@link Variant}
     * @return no content or 404
     * @statuscode 204 The Variant successfully deleted
     * @statuscode 404 The requested Variant resource exists but it is not of the given type
     * @statuscode 404 The requested Variant resource does not exist
     */
    @DELETE
    @Path("/{variantId}")
    public Response deleteVariant(@PathParam("pushAppID") String pushApplicationID,
                                  @PathParam("variantId") String variantId) {
        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forPushApplications().notFound().build()).build();
        }

        Variant variant = variantService.findByVariantID(variantId);

        if (variant == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
        }

        if (!type.isInstance(variant)) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forVariants().notFound().build()).build();
        }

        logger.trace("Deleting: {}", variant.getClass().getSimpleName());

        variantService.removeVariant(variant);
        return Response.noContent().build();
    }

    protected Set<T> getVariants(PushApplication application) {
        return application.getVariants().stream()
                .filter(variant -> type.isAssignableFrom(variant.getClass()))
                .map(variant -> (T) variant)
                .collect(Collectors.toSet());
    }

}
