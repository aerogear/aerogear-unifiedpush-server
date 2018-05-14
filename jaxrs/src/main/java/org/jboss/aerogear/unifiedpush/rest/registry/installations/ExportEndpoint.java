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

import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.jboss.resteasy.annotations.GZIP;

import javax.validation.Validator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/export")
public class ExportEndpoint extends AbstractBaseEndpoint {

    // required for RESTEasy
    public ExportEndpoint() {
    }

    // required for tests
    protected ExportEndpoint(Validator validator, SearchManager searchManager) {
        super(validator, searchManager);
    }

    /**
     * Endpoint for exporting as JSON file device installations for a given variant.
     * Only Keycloak authenticated can access it
     *
     * @param variantId the variant ID
     * @return          list of {@link org.jboss.aerogear.unifiedpush.api.Installation}s
     */
    @GET
    @Path("/{variantId}/installations/")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    public Response exportInstallations(@PathParam("variantId") String variantId) {
        return Response.ok(getSearch().findAllInstallationsByVariantForDeveloper(variantId, 0, Integer.MAX_VALUE, null).getResultList()).build();
    }

}
