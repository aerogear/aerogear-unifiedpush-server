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

import org.jboss.aerogear.security.auth.LoggedUser;
import org.jboss.aerogear.security.authz.Secure;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Stateless
@TransactionAttribute
@Path("/applications/{variantID}/installations/")
@Secure( { "developer", "admin" })
public class InstallationManagementEndpoint {

    @Inject
    private ClientInstallationService clientInstallationService;

    @Inject
    @LoggedUser
    private Instance<String> loginName;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findInstallations(@PathParam("variantID") String variantId) {

        //Find the installations using the variantID
        List<Installation> installations = clientInstallationService.findInstallationsByVariant(variantId, loginName.get());

        if (installations.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
        }

        return Response.ok(installations).build();
    }

    @GET
    @Path("/{installationID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findInstallation(@PathParam("variantID") String variantId, @PathParam("installationID") String installationId) {

        Installation installation = clientInstallationService.findById(installationId);

        if (installation == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Installation").build();
        }

        return Response.ok(installation).build();
    }

    @PUT
    @Path("/{installationID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateInstallation(Installation entity, @PathParam("variantID") String variantId, @PathParam("installationID") String installationId) {

        Installation installation = clientInstallationService.findById(installationId);

        if (installation == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Installation").build();
        }

        clientInstallationService.updateInstallation(installation, entity);

        return Response.noContent().build();

    }

    @DELETE
    @Path("/{installationID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeInstallation(@PathParam("variantID") String variantId, @PathParam("installationID") String installationId) {

        Installation installation = clientInstallationService.findById(installationId);

        if (installation == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Installation").build();
        }

        // remove it
        clientInstallationService.removeInstallation(installation);

        return Response.noContent().build();
    }
}
