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

import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.document.DocumentDeployMessage;
import org.jboss.aerogear.unifiedpush.document.MessagePayload;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.NotificationRouter;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.rest.AbstractEndpoint;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.annotations.PATCH;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.rest.util.PushAppAuthHelper;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import com.qmino.miredot.annotations.BodyType;
import com.qmino.miredot.annotations.ReturnType;

@Path("/sender")
public class PushNotificationSenderEndpoint extends AbstractEndpoint {

    private final AeroGearLogger logger = AeroGearLogger.getInstance(PushNotificationSenderEndpoint.class);
    @Inject
    private PushApplicationService pushApplicationService;
    @Inject
    private NotificationRouter notificationRouter;
	@Inject
	private DocumentService documentService;

    /**
     * RESTful API for sending Push Notifications.
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>PushApplicationID:masterSecret</code>).
     * <p/><p/>
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
     *
     * @HTTP 202 (Accepted) Indicates the Job has been accepted and is being process by the AeroGear UnifiedPush Server.
     * @HTTP 401 (Unauthorized) The request requires authentication.
     * @RequestHeader aerogear-sender The header to identify the used client. If the header is not present, the standard "user-agent" header is used.
     *
     * @param message   message to send
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
    @BodyType("org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage")
    @ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
    public Response send(final InternalUnifiedPushMessage message, @Context HttpServletRequest request) {

        final PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request, pushApplicationService);
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
        logger.fine(String.format("Push Message Request from [%s] API was internally submitted for further processing", message.getClientIdentifier()));

        return Response.status(Status.ACCEPTED).entity(EmptyJSON.STRING).build();
    }

	/**
	 * POST accept large payload and stores it for later retrieval by a client
	 * of the push application.
	 *
	 * @param pushAppId
	 *            id of
	 *            {@link org.jboss.aerogear.unifiedpush.api.PushApplication}
	 * @aliasToDocument a map between aliases and documents.
	 *
	 * @statuscode 401 if unauthorized for this push application
	 * @statuscode 500 if request failed
	 * @statuscode 200 upon success
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/payload")
	public Response sendLargePayload(DocumentDeployMessage payloadRequest, @Context HttpServletRequest request) {
		final PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request,
				pushApplicationService);
		return sendLargePayload(pushApplication, payloadRequest, false, request);

	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/payload")
	public Response updateLargePayload(DocumentDeployMessage payloadRequest, @Context HttpServletRequest request) {
		final PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request,
				pushApplicationService);
		return sendLargePayload(pushApplication, payloadRequest, true, request);
	}

	private Response sendLargePayload(PushApplication pushApplication, DocumentDeployMessage payloadRequest, boolean override, @Context HttpServletRequest request) {
		if (pushApplication == null) {
			return Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
					.entity("Unauthorized Request").build();
		}

		if (payloadRequest.getAliasPayload().isEmpty() && payloadRequest.getGlobalPayload() == null) {
			return appendAllowOriginHeader(
					Response.status(Status.BAD_REQUEST).entity("At list one MessagePayload should be provided"),
					request);

		}

		try {
			// Save aliases payload
			if (!payloadRequest.getAliasPayload().isEmpty()) {
				boolean pushGlobalMessage = false;

				if (payloadRequest.getGlobalPayload().getPushMessage() != null)
					pushGlobalMessage = true;

				for (Map.Entry<String, MessagePayload> entry : payloadRequest.getAliasPayload().entrySet()) {
					documentService.savePayload(pushApplication, entry.getKey(), entry.getValue().getPayload(),
							DocumentMetadata.getQualifier(payloadRequest.getQualifier()), null, false);

					// Push notification
					if (!pushGlobalMessage) {
						push(entry.getValue().getPushMessage(), pushApplication, request);
					}
				}
			}

			// Save global payload
			if (payloadRequest.getGlobalPayload() != null && payloadRequest.getGlobalPayload().getPayload() != null
					&& payloadRequest.getGlobalPayload().getPayload().length() != 0) {

				documentService.savePayload(pushApplication, DocumentMetadata.NULL_ALIAS,
						payloadRequest.getGlobalPayload().getPayload(),
						DocumentMetadata.getQualifier(payloadRequest.getQualifier()), null, false);
			}

			// Send global push
			if (payloadRequest.getGlobalPayload().getPushMessage() != null) {
				push(payloadRequest.getGlobalPayload().getPushMessage(), pushApplication, request);
			}
		} catch (Exception e) {
			logger.severe("Cannot store payload and send notification", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

		return Response.ok(EmptyJSON.STRING).build();
	}

	private void push(UnifiedPushMessage pushMessage, PushApplication pushApplication, HttpServletRequest request) {
		if (pushMessage != null) {
			InternalUnifiedPushMessage message = new InternalUnifiedPushMessage(pushMessage);
			// TODO: refactor into common class shared with
			// PushNotificationSenderEndpoint
			// submit http request metadata:
			message.setIpAddress(HttpRequestUtil.extractIPAddress(request));
			// add the client identifier
			message.setClientIdentifier(HttpRequestUtil.extractAeroGearSenderInformation(request));

			notificationRouter.submit(pushApplication, message);
		}
	}
}
