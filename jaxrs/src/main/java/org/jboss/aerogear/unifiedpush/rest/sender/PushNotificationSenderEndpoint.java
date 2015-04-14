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
import org.jboss.aerogear.unifiedpush.message.NotificationRouter;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/sender")
public class PushNotificationSenderEndpoint {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(PushNotificationSenderEndpoint.class);
    @Inject
    private PushApplicationService pushApplicationService;
    @Inject
    private NotificationRouter notificationRouter;

    /**
     * RESTful API for sending Push Notifications.
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>PushApplicationID:masterSecret</code>).
     * <p/><p/>
     *
     * Messages are submitted as flexible JSON maps, like:
     * <pre>
     * curl -u "PushApplicationID:MasterSecret"
     *   -v -H "Accept: application/json" -H "Content-type: application/json"
     *   -X POST
     *   -d '{
     *     "message": {
     *      "alert": "HELLO!",
     *      "sound": "default",
     *      "badge": 2,
     *      "user-data": {
     *          "key": "value",
     *          "key2": "other value"
     *      },
     *      "windows": {
     *          "type": "tile",
     *          "duration": "short",
     *          "badge": "alert",
     *          "tileType": "TileWideBlockAndText01",
     *          "images": ["Assets/test.jpg", "Assets/background.png"],
     *          "textFields": ["foreground text"]
     *      },
     *      "apns": {
     *          "title" : "someTitle",
     *          "action-category": "some value",
     *          "content-available": true,
     *          "action" : "someAction",
     *          "url-args" :["args1","arg2"],
     *          "localized-title-key" : "some value",
     *          "localized-title-arguments" : ["args1","arg2"]
     *      }
     *      "simple-push": "version=123"
     *     },
     *     "criteria": {
     *         "alias": [ "someUsername" ],
     *         "deviceType": [ "someDevice" ],
     *         "categories": [ "someCategories" ],
     *         "variants": [ "someVariantIDs" ]
     *     },
     *     "config": {
     *         "ttl": 3600
     *     }
     *   }'
     *   https://SERVER:PORT/CONTEXT/rest/sender
     * </pre>
     *
     * Details about the Message Format can be found HERE!
     *
     * @HTTP 202 (Accepted) Indicates the Job has been accepted and is being process by the AeroGear UnifiedPush Server.
     * @HTTP 401 (Unauthorized) The request requires authentication.
     * @HTTP 404 (Not Found) The requested PushApplication resource does not exist.
     * @RequestHeader aerogear-sender The header to identify the used client. If the header is not present, the standard "user-agent" header is used.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response send(final UnifiedPushMessage message, @Context HttpServletRequest request) {

        final PushApplication pushApplication = loadPushApplicationWhenAuthorized(request);
        if (pushApplication == null) {
            return Response.status(Status.UNAUTHORIZED)
                    .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                    .entity("Unauthorized Request")
                    .build();
        }

        // submit http request metadata:
        message.setIpAddress(HttpRequestUtil.extractIPAddress(request));

        // add the client identifier
        message.setClientIdentifier(HttpRequestUtil.extractAeroGearSenderInformation(request));

        // submitted to EJB:
        notificationRouter.submit(pushApplication, message);
        logger.fine("Message sent by: '" + message.getClientIdentifier() + "'");
        logger.info("Message submitted to PushNetworks for further processing");

        return Response.status(Status.ACCEPTED)
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
