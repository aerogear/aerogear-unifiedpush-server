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

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.aerogear.connectivity.cdi.event.PushMessageEventDispatcher;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.rest.sender.messages.BroadcastMessage;
import org.aerogear.connectivity.rest.sender.messages.SelectiveSendMessage;
import org.aerogear.connectivity.service.PushApplicationService;

@Stateless
@Path("/sender")
@TransactionAttribute
public class PushNotificationSenderEndpoint {

    @Inject private Logger logger;
    @Inject private PushApplicationService pushApplicationService;
    @Inject private PushMessageEventDispatcher dispatcher;
    
    @POST
    @Path("/broadcast/{pushApplicationID}")
    @Consumes("application/json")
    public Response broadcast(BroadcastMessage message, @PathParam("pushApplicationID") String pushApplicationID) {
        PushApplication pushApplication = pushApplicationService.findByPushApplicationID(pushApplicationID);

        if (pushApplication == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        dispatcher.dispatchBroadcastMessage(pushApplication, message);
        logger.info("Message submitted to PushNetworks");

        return Response.status(Status.OK)
                .entity("Job submitted").build();
    }

    @POST
    @Path("/selected/{pushApplicationID}")
    @Consumes("application/json")
    public Response selectedSender(SelectiveSendMessage message, @PathParam("pushApplicationID") String pushApplicationID) {
        PushApplication pushApplication = pushApplicationService.findByPushApplicationID(pushApplicationID);

        if (pushApplication == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        dispatcher.dispatchSelectedSendMessage(pushApplication, message);
        logger.info("Message submitted to PushNetworks");

        return Response.status(Status.OK)
                .entity("Job submitted").build();
    }
}
