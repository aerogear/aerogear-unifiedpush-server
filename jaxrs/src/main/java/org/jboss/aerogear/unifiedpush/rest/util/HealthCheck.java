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
package org.jboss.aerogear.unifiedpush.rest.util;

import org.jboss.aerogear.unifiedpush.message.HealthNetworkService;
import org.jboss.aerogear.unifiedpush.service.HealthDBService;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthDetails;
import org.jboss.aerogear.unifiedpush.service.impl.health.HealthStatus;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A class to test 'health' of the server
 */
@Path("/sys/info")
public class HealthCheck {

    @Inject
    private HealthDBService healthDBService;

    @Inject
    private HealthNetworkService healthNetworkService;

    @GET
    @Path("/health")
    @Produces(MediaType.APPLICATION_JSON)
    public HealthStatus health() throws ExecutionException, InterruptedException {
        final HealthStatus status = new HealthStatus();

        final Future<HealthDetails> dbStatus = healthDBService.dbStatus();
        final Future<List<HealthDetails>> networkStatus = healthNetworkService.networkStatus();

        status.add(dbStatus.get());
        for (HealthDetails details : networkStatus.get()) {
            status.add(details);
        }

        return status;
    }

}
