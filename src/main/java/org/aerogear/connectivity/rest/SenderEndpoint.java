/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
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

package org.aerogear.connectivity.rest;

import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.service.PushApplicationService;
import org.aerogear.connectivity.service.SenderService;

@Stateless
@Path("/sender")
@TransactionAttribute
public class SenderEndpoint {
    @Inject
    private PushApplicationService pushApplicationService;
    
    @Inject SenderService senderService;

    @POST
    @Path("/broadcast/{id}")
    @Consumes("application/json")
    public Response broadcast(Map<String, String> message, @PathParam("id") String pushApplicationId) {

        PushApplication pushApp = pushApplicationService.findPushApplicationById(pushApplicationId);
        senderService.broadcast(pushApp, message);

        return Response.status(200)
                .entity("Job submitted").build();
    }
}
