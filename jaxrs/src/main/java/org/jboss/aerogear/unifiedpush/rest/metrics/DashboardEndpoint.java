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

import org.jboss.aerogear.unifiedpush.service.dashboard.Application;
import org.jboss.aerogear.unifiedpush.service.dashboard.ApplicationVariant;
import org.jboss.aerogear.unifiedpush.service.dashboard.DashboardData;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/metrics/dashboard")
public class DashboardEndpoint {

    @Inject
    private SearchManager service;

    /**
     * GET dashboard data
     *
     * @param request the request
     *
     * @return  {@link DashboardData} for the given user
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response totalApplications(@Context HttpServletRequest request) {
        final DashboardData dataForUser =  service.getSearchService().loadDashboardData();

        return Response.ok(dataForUser).build();
    }

    /**
     * GET application variants
     *
     * @param request the request
     *
     * @return  list of {@link ApplicationVariant}s
     */
    @GET
    @Path("/warnings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariantsWithWarnings(@Context HttpServletRequest request) {
        final List<ApplicationVariant> variantsWithWarnings = service.getSearchService().getVariantsWithWarnings();

        return Response.ok(variantsWithWarnings).build();
    }

    /**
     * GET active applications
     *
     * @param request the request
     *
     * @param count number of active applications, default value = 3
     * @return      list of active {@link Application}s
     */
    @GET
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestActivity(@QueryParam("count") @DefaultValue("3") int count, @Context HttpServletRequest request) {
        final List<Application> latestActivity = service.getSearchService().getLatestActivity(count);

        return Response.ok(latestActivity).build();
    }
}
