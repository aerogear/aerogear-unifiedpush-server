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

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.auth.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.NotificationRouter;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.rest.util.error.ErrorBuilder;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.metrics.PrometheusExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/sender")
public class PushNotificationSenderEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationSenderEndpoint.class);

    @Inject
    private PushApplicationService pushApplicationService;
    @Inject
    private NotificationRouter notificationRouter;

    /**
     * RESTful API for sending Push Notifications.
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>PushApplicationID:masterSecret</code>).
     * <p>
     *
     * Messages are submitted as flexible JSON maps. Below is a simple example:
     * <pre>
     * curl -u "PushApplicationID:MasterSecret"
     *   -v -H "Accept: application/json" -H "Content-type: application/json"
     *   -X POST
     *   -d '{
     *     "message": {
     *      "alert": "HELLO!",
     *      "sound": "default",
     *      "user-data": {
     *          "key": "value",
     *      }
     *   }'
     *   https://SERVER:PORT/CONTEXT/rest/sender
     * </pre>
     *
     * Details about the Message Format can be found HERE!
     * <p>
     *
     * <b>Request Header</b> {@code aerogear-sender} uses to identify the used client. If the header is not present, the standard "user-agent" header is used.
     *
     * @param message   message to send
     * @param request the request
     * @return          empty JSON body
     *
     * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server" (only for 401 response)
     *
     * @statuscode 202 Indicates the Job has been accepted and is being process by the AeroGear UnifiedPush Server
     * @statuscode 401 The request requires authentication
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response send(final InternalUnifiedPushMessage message, @Context HttpServletRequest request) {

        PrometheusExporter.instance().increaseTotalPushRequests();

        final PushApplication pushApplication = loadPushApplicationWhenAuthorized(request);
        if (pushApplication == null) {
            return Response.status(Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                    .entity(ErrorBuilder.forAuth().unauthorized().build())
                    .build();
        }

        // submit http request metadata:
        message.setIpAddress(HttpRequestUtil.extractIPAddress(request));

        // add the client identifier
        message.setClientIdentifier(HttpRequestUtil.extractAeroGearSenderInformation(request));

        // submitted to EJB:
        notificationRouter.submit(pushApplication, message);
        logger.debug("Push Message Request from [{}] API was internally submitted for further processing", message.getClientIdentifier());

        return Response.status(Status.ACCEPTED).entity(EmptyJSON.STRING).build();
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
