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
package org.jboss.aerogear.unifiedpush.rest.util;

import org.jboss.aerogear.unifiedpush.message.HealthNetworkService;
import org.jboss.aerogear.unifiedpush.rest.util.error.ErrorBuilder;
import org.jboss.aerogear.unifiedpush.service.HealthDBService;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthDetails;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthStatus;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * A class to test 'health' of the server
 */
@Path("/sys/info")
public class HealthCheck {

    @Inject
    private HealthDBService healthDBService;

    @Inject
    private HealthNetworkService healthNetworkService;

    /**
     * Get health status
     *
     * @return {@link HealthStatus} with details
     *
     * @throws ExecutionException   The computation of health status threw an exception
     * @throws InterruptedException The thread, which compute health status, was interrupted
     */
    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public HealthStatus health() throws ExecutionException, InterruptedException {
        final HealthStatus status = new HealthStatus();

        final Future<HealthDetails> dbStatus = healthDBService.dbStatus();
        final Future<List<HealthDetails>> networkStatus = healthNetworkService.networkStatus();

        status.add(dbStatus.get());
        networkStatus.get().forEach(status::add);

        return status;
    }

    /**
     * Simple Ping endpoint to check if the UPS is running as expected
     *
     * @return simple OK string if the server is running
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        return Response.ok("OK").build();
    }


    /**
     * Endpoint to verify scm and version details
     *
     * @return {@link Attributes}
     * @statuscode 200 Successful response for your request with manifest's attributes
     * @statuscode 404 Not found version information
     */
    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public Response manifestDetails(@Context HttpServletRequest request) {

        final ServletContext context = request.getSession().getServletContext();

        try (final InputStream manifestStream = context.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            return Response.ok(new Manifest(manifestStream).getMainAttributes()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity(ErrorBuilder.forHealthCheck().noVersion().build()).build();
        }
    }
}
