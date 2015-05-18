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


import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.dto.Count;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.jboss.resteasy.spi.Link;
import org.jboss.resteasy.spi.LinkHeader;


@Path("/applications/{variantID}/installations/")
public class InstallationManagementEndpoint {
    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 25;

    @Inject
    private SearchManager searchManager;

    @Inject
    private ClientInstallationService clientInstallationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findInstallations(@PathParam("variantID") String variantId, @QueryParam("page") Integer page,
                                      @QueryParam("per_page") Integer pageSize, @Context UriInfo uri) {
        if (pageSize != null) {
            pageSize = Math.min(MAX_PAGE_SIZE, pageSize);
        } else {
            pageSize = DEFAULT_PAGE_SIZE;
        }

        if (page == null) {
            page = 0;
        }

        //Find the variant using the variantID
        if (!searchManager.getSearchService().existsVariantIDForDeveloper(variantId)) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Variant").build();
        }

        //Find the installations using the variantID
        PageResult<Installation, Count> pageResult = searchManager.getSearchService().findAllInstallationsByVariantForDeveloper(variantId, page, pageSize);

        final long totalPages = pageResult.getAggregate().getCount() / pageSize;
        LinkHeader header = getLinkHeader(page, totalPages, uri);

        return Response.ok(pageResult.getResultList())
                .header("Link", header.toString())
                .header("total", pageResult.getAggregate().getCount())
                .build();
    }

    LinkHeader getLinkHeader(Integer page, long totalPages, UriInfo uri) {
        LinkHeader header = new LinkHeader();

        if (page != 0) {
            header.addLink(buildLink("prev", page - 1, uri));
            header.addLink(buildLink("first", 0, uri));
        }

        if (page < totalPages) {
            header.addLink(buildLink("next", page + 1, uri));
            header.addLink(buildLink("last", totalPages, uri));
        }
        return header;
    }

    private Link buildLink(String rel, long pageNo, UriInfo uri) {
        Link link = new Link();
        link.setHref(uri.getAbsolutePathBuilder().queryParam("page", pageNo).build().toASCIIString());
        link.setRelationship(rel);
        return link;
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
