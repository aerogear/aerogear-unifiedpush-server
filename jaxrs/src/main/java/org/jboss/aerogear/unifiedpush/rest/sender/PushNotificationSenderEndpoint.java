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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiFunction;

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

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.User;
import org.jboss.aerogear.unifiedpush.message.Criteria;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.NotificationRouter;
import org.jboss.aerogear.unifiedpush.rest.AbstractEndpoint;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.rest.util.PushAppAuthHelper;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

import com.qmino.miredot.annotations.BodyType;
import com.qmino.miredot.annotations.ReturnType;

@Controller
@Path("/sender")
public class PushNotificationSenderEndpoint extends AbstractEndpoint {
    private final Logger logger = LoggerFactory.getLogger(PushNotificationSenderEndpoint.class);

    @Inject
    private PushApplicationService pushApplicationService;
    @Inject
    private NotificationRouter notificationRouter;
    @Inject
    private AliasService aliasService;

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
     * <b>Request Header</b> {@code aerogear-sender} uses to identify the used client. If the header is not present,
     * the standard &quot;user-agent&quot; header is used.
     *
     * @param message   message to send
     * @param request the request
     * @return          empty JSON body
     *
     * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server" (only for 401 response)
     *
     * @statuscode 202 Indicates the Job has been accepted and is being process by the AeroGear UnifiedPush Server
     * @statuscode 401 The request requires authentication
     * @statuscode 500 Internal error resolving UUID(s) into aliases (if so requested via &quot;resolveUUID=true&quot;)
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BodyType("org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage")
    @ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
    public Response sendAliases(InternalUnifiedPushMessage message, @Context HttpServletRequest request) {
        return send(message, request, (app, upm) -> upm);
    }

    /**
     * Same as &quot;sendAliases&quot; only assume the criteria contains UUID(s) that need conversion to aliases
     */
    @POST
    @Path("/uuids")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @BodyType("org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage")
    @ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
    public Response sendUUIDs(InternalUnifiedPushMessage message, @Context HttpServletRequest request) {
        return send(message, request, (app, msg) -> {
            Criteria criteria = msg.getCriteria();
            List<String> aliases = resolveAliasesFromUUIDs(
                UUID.fromString(app.getPushApplicationID()), criteria.getAliases());
            criteria.setAliases(aliases);
            return msg;
        });
    }

    // Preserve backward compatibility
    private Response send(InternalUnifiedPushMessage message, @Context HttpServletRequest request,
              BiFunction<? super PushApplication, ? super InternalUnifiedPushMessage, ? extends InternalUnifiedPushMessage> processor) {
        PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request, pushApplicationService);
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

        try {
            message = processor.apply(pushApplication, message);
        } catch (RuntimeException e) {
            logger.error("Failed (" + e.getClass().getSimpleName() + ") to pre-process push message: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                .entity(e.getClass().getSimpleName() + ": " + e.getMessage())
                .build();
        }

        // submitted to EJB:
        notificationRouter.submit(pushApplication, message);
        logger.debug(String.format("Push Message Request from [%s] API was internally submitted for further processing", message.getClientIdentifier()));

        return Response.status(Status.ACCEPTED).entity(EmptyJSON.STRING).build();
    }

    private List<String> resolveAliasesFromUUIDs(UUID appId, Collection<String> uuids) {
        if (CollectionUtils.isEmpty(uuids)) {
            return Collections.emptyList();
        }

        // If a UUID has the same mail in several cases then count them as 1 alias
        Collection<String> aliases = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String userId : uuids) {
            Map<String, User.AliasType> userAliases = aliasService.findAll(appId, UUID.fromString(userId));
            if ((userAliases == null) || (userAliases.size() <= 0)) {
                logger.warn("No aliases found for user=" + userId + " of application=" + appId);
                continue;
            }

            aliases.addAll(userAliases.keySet());
        }

        if (aliases.isEmpty()) {
            logger.warn("Could not resolve any of the aliases for application=" + appId);
            return Collections.emptyList();
        }

        return new ArrayList<>(aliases);
    }
}
