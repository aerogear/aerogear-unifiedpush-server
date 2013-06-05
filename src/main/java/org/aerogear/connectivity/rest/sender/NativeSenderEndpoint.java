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

package org.aerogear.connectivity.rest.sender;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.aerogear.connectivity.cdi.event.PushMessageEventDispatcher;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.service.PushApplicationService;

@Stateless
@Path("/sender")
@TransactionAttribute
public class NativeSenderEndpoint {

    @Inject private Logger logger;
    @Inject private PushApplicationService pushApplicationService;
    @Inject private PushMessageEventDispatcher dispatcher;
    
    @POST
    @Path("/broadcast/{pushApplicationID}")
    @Consumes("application/json")
    public Response broadcast(LinkedHashMap<String, ? extends Object> message, @PathParam("pushApplicationID") String pushApplicationID) {
        PushApplication pushApplication = pushApplicationService.findByPushApplicationID(pushApplicationID);
        dispatcher.dispatchBroadcastMessage(pushApplication, message);
        logger.info("Message submitted to PushNetworks");

        return Response.status(200)
                .entity("Job submitted").build();
    }

    @POST
    @Path("/selected/{pushApplicationID}")
    @Consumes("application/json")
    public Response selectedSender(LinkedHashMap<String, ? extends Object> message, @PathParam("pushApplicationID") String pushApplicationID) {
        PushApplication pushApplication = pushApplicationService.findByPushApplicationID(pushApplicationID);
        // read the receivers of the message:
        List<String> identifiers = (List<String>) message.get("alias");
        // extract the payload
        LinkedHashMap<String, ? extends Object> payload = (LinkedHashMap<String, ? extends Object>) message.get("message");
        dispatcher.dispatchSelectedSendMessage(pushApplication, payload, identifiers);
        
        return Response.status(200)
                .entity("Job submitted").build();
    }
}
