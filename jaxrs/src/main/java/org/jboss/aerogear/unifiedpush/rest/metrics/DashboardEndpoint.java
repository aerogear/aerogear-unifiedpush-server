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

import org.jboss.aerogear.unifiedpush.service.dashboard.ApplicationVariant;
import org.jboss.aerogear.unifiedpush.service.dashboard.DashboardData;
import org.jboss.aerogear.unifiedpush.service.dashboard.DashboardService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil.extractUsername;

@Stateless
@Path("/metrics/dashboard")
public class DashboardEndpoint {

    @Inject
    private DashboardService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response totalApplications(@Context HttpServletRequest request) {
        final String principalName = extractUsername(request);
        final DashboardData dataForUser =  service.loadDashboardData(principalName);

        return Response.ok(dataForUser).build();
    }

    @GET
    @Path("/warnings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVariantsWithWarnings(@Context HttpServletRequest request) {
        final String principalName = extractUsername(request);
        final List<ApplicationVariant> variantsWithWarnings = service.getVariantsWithWarnings(principalName);

        return Response.ok(variantsWithWarnings).build();
    }

    @GET
    @Path("/active")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTopThreeVariants(@Context HttpServletRequest request) {
        final String principalName = extractUsername(request);
        final List<ApplicationVariant> variantsWithWarnings = service.getTopThreeBusyVariants(principalName);

        return Response.ok(variantsWithWarnings).build();
    }
}
