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
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage.DocumentType;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.document.DocumentDeployRequest;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.NotificationRouter;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.rest.util.PushAppAuthHelper;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

import com.qmino.miredot.annotations.ReturnType;

@Path("/applicationsData")
public class PushApplicationDataEndpoint extends AbstractBaseEndpoint {

	@Inject
	private PushApplicationService pushAppService;

	@Inject
	private DocumentService documentService;

	@Inject
	private NotificationRouter notificationRouter;

	/**
	 * Overwrites existing categories and properties of the push application
	 * with the given data
	 * 
	 * @param pushApplicationID
	 *            id of {@linkplain PushApplication}
	 * @param categoryData
	 *            map from category name to its list of property names
	 */
	@POST
	@Path("/{pushAppID}/aliases")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response updateAliases(@PathParam("pushAppID") String pushApplicationID, List<String> aliasData,
			@Context HttpServletRequest request) {
		final PushApplication pushApp = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request, pushAppService);
		if (pushApp == null) {
			return Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
					.entity("Unauthorized Request").build();
		}

		try {
			pushAppService.updateAliasesAndInstallations(pushApp, aliasData);
			return Response.ok(EmptyJSON.STRING).build();
		} catch (Exception e) {
			logger.severe("Cannot update aliases", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/{pushAppID}/document")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response retrieveDocumentsForPushApp(@PathParam("pushAppID") String pushApplicationID,
			@QueryParam("date") Long date, @QueryParam("type") DocumentType type, @Context HttpServletRequest request) {
		final PushApplication pushApp = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request, pushAppService);
		if (pushApp == null) {
			return Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
					.entity("Unauthorized Request").build();
		}

		try {
			documentService.getPushApplicationDocuments(pushApp, type, new Date(date));
			return Response.ok(EmptyJSON.STRING).build();
		} catch (Exception e) {
			logger.severe("Cannot retrieve documents for push app", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	/**
	 * POST deploys a file and stores it for later retrieval by a client of the
	 * push application.
	 *
	 * @param pushAppId
	 *            id of
	 *            {@link org.jboss.aerogear.unifiedpush.api.PushApplication}
	 * @aliasToDocuments a map between aliases and a list of documents to save
	 *                   for each one
	 *
	 * @statuscode 401 if unauthorized for this push application
	 * @statuscode 500 if request failed
	 * @statuscode 200 upon success
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{pushAppID}/document")
	public Response deployDocumentsForAlias(@PathParam("pushAppID") String pushApplicationID,
			DocumentDeployRequest deployRequest, @Context HttpServletRequest request) {

		final PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request,
				pushAppService);
		if (pushApplication == null) {
			return Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
					.entity("Unauthorized Request").build();
		}

		if (!deployRequest.getAliasToDocuments().isEmpty()) {
			try {
				documentService.saveForAliases(pushApplication, deployRequest.getAliasToDocuments(),
						DocumentMessage.getDocumentQualifier(deployRequest.getType()));

				final UnifiedPushMessage pushMessage = deployRequest.getPushMessage();
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
			} catch (Exception e) {
				logger.severe("Cannot deploy file for alias", e);
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

		return Response.ok(EmptyJSON.STRING).build();
	}
}
