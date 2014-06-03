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
package org.jboss.aerogear.unifiedpush.rest.sender;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.message.SenderService;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

@Stateless
@Path("/sender")
public class PushNotificationSenderEndpoint {

    private final Logger logger = Logger.getLogger(PushNotificationSenderEndpoint.class.getName());
    @Inject
    private PushApplicationService pushApplicationService;
    @Inject
    private SenderService senderService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(final Map<String, Object> message, @Context HttpServletRequest request) {

        final PushApplication pushApplication = loadPushApplicationWhenAuthorized(request);
        if (pushApplication == null) {
            return Response.status(Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                    .entity("Unauthorized Request")
                    .build();
        }

        // transform map to service object:
        final UnifiedPushMessage payload = new UnifiedPushMessage(message);

        // submit http request metadata:
        payload.setIpAddress(HttpRequestUtil.extractIPAddress(request));

        // add the client identifier
        payload.setClientIdentifier(HttpRequestUtil.extractAeroGearSenderInformation(request));

        // submitted to @Async EJB:
        senderService.send(pushApplication, payload);
        logger.log(Level.FINE, "Message sent by: '" + payload.getClientIdentifier() + "'");
        logger.log(Level.INFO, "Message submitted to PushNetworks for further processing");

        return Response.status(Status.OK)
                .entity("Job submitted").build();
    }

    /**
     * returns application if the masterSecret is valid for the request PushApplicationEntity
     */
    private PushApplication loadPushApplicationWhenAuthorized(HttpServletRequest request) {
        // extract the pushApplicationID and its secret from the HTTP Basic header:
        String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
        String pushApplicationID = credentials[0];
        String secret = credentials[1];

        final PushApplication pushApplication = pushApplicationService.findByPushApplicationID(pushApplicationID);
        if (pushApplication != null && pushApplication.getMasterSecret().equals(secret)) {
            return pushApplication;
        }

        // unauthorized...
        return null;
    }
}
