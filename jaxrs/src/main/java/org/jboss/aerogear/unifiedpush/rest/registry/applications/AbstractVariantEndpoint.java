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

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Abstract base class for all the concrete variant endpoints. Shares common
 * functionality.
 */
public abstract class AbstractVariantEndpoint extends AbstractBaseEndpoint {

    @Inject
    protected PushApplicationService pushAppService;

    @Inject
    protected GenericVariantService variantService;

    /**
     * Secret Reset
     *
     * @param variantId id of {@link Variant}
     * @return          {@link Variant} with new secret
     *
     * @statuscode 200 The secret of Variant reset successfully
     * @statuscode 404 The requested Variant resource does not exist or it is not of the given type
     */
    protected <T extends Variant> Response doResetSecret(String variantId, Class<T> type) {

        Variant variant = variantService.findByVariantID(variantId);

        if (variant == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
        }

        if (!type.isInstance(variant)) {
            return Response.status(Response.Status.NOT_FOUND).entity("Requested Variant is of another type/platform").build();
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
     * @return          requested {@link Variant}
     *
     * @statuscode 404 The requested Variant resource does not exist or it is not of the given type
     */
    protected <T extends Variant> Response doFindVariantById(String variantId, Class<T> type) {

        Variant variant = variantService.findByVariantID(variantId);

        if (variant == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
        }

        if (!type.isInstance(variant)) {
            return Response.status(Response.Status.NOT_FOUND).entity("Requested Variant is of another type/platform").build();
        }

        return Response.ok(variant).build();
    }

    /**
     * Delete Variant
     *
     * @param variantId id of {@link Variant}
     *
     * @return no content or 404
     *
     * @statuscode 204 The Variant successfully deleted
     * @statuscode 404 The requested Variant resource does not exist or it is not of the given type
     */
    protected <T extends Variant> Response doDeleteVariant(String variantId, Class<T> type) {

        Variant variant = variantService.findByVariantID(variantId);

        if (variant == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
        }

        if (!type.isInstance(variant)) {
            return Response.status(Response.Status.NOT_FOUND).entity("Requested Variant is of another type/platform").build();
        }

        logger.trace("Deleting: {}", variant.getClass().getSimpleName());

        variantService.removeVariant(variant);
        return Response.noContent().build();
    }

    protected <T extends Variant> Set<T> getVariantsByType(PushApplication application, Class<T> type) {
        Objects.requireNonNull(type, "type");
        return application.getVariants().stream()
                .filter(variant -> variant.getClass().equals(type))
                .map(variant -> (T) variant)
                .collect(Collectors.toSet());
    }

}
