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
package org.jboss.aerogear.unifiedpush.rest.metrics;

import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Stateless
@Path("/metrics/messages")
public class PushMetricsEndpoint {

    @Inject
    private PushMessageMetricsService metricsService;

    @GET
    @Path("/application/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pushMessageInformationPerApplication(
            @Context HttpServletRequest request,
            @PathParam("id") String id,
            @QueryParam("sort") String sorting) {

        if (id == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested information").build();
        }

        List<PushMessageInformation> messageInformations = metricsService.findAllForPushApplication(id, HttpRequestUtil.extractSortingQueryParamValue(sorting));

        return Response.ok(messageInformations).build();
    }

    @GET
    @Path("/variant/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pushMessageInformationPerVariant(
            @Context HttpServletRequest request,
            @PathParam("id") String id,
            @QueryParam("sort") String sorting) {

        if (id == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested information").build();
        }

        List<PushMessageInformation> messageInformations = metricsService.findAllForVariant(id, HttpRequestUtil.extractSortingQueryParamValue(sorting));

        return Response.ok(messageInformations).build();
    }
}
