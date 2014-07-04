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
import org.jboss.aerogear.unifiedpush.dao.PageResult;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil.extractSortingQueryParamValue;

@Stateless
@Path("/metrics/messages")
public class PushMetricsEndpoint {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 25;

    @Inject
    private PushMessageMetricsService metricsService;

    @GET
    @Path("/application/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pushMessageInformationPerApplication(
            @PathParam("id") String id,
            @QueryParam("page") Integer page,
            @QueryParam("per_page") Integer pageSize,
            @QueryParam("sort") String sorting) {

        pageSize = parsePageSize(pageSize);

        if (page == null) {
            page = 0;
        }

        if (id == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested information").build();
        }

        PageResult<PushMessageInformation> pageResult =
                metricsService.findAllForPushApplication(id, extractSortingQueryParamValue(sorting), page, pageSize);

        return Response.ok(pageResult.getResultList())
                .header("total", pageResult.getCount()).build();
    }

    @GET
    @Path("/variant/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response pushMessageInformationPerVariant(
            @PathParam("id") String id,
            @QueryParam("page") Integer page,
            @QueryParam("per_page") Integer pageSize,
            @QueryParam("sort") String sorting) {

        pageSize = parsePageSize(pageSize);

        if (page == null) {
            page = 0;
        }

        if (id == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested information").build();
        }

        PageResult<PushMessageInformation> pageResult =
                metricsService.findAllForVariant(id, extractSortingQueryParamValue(sorting), page, pageSize);

        return Response.ok(pageResult.getResultList())
                .header("total", pageResult.getCount()).build();
    }

    private Integer parsePageSize(Integer pageSize) {
        if (pageSize != null) {
            pageSize = Math.min(MAX_PAGE_SIZE, pageSize);
        } else {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }
}
