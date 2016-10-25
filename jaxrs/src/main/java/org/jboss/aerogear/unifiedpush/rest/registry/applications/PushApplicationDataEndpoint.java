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

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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

import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.document.DocumentDeployRequest;
import org.jboss.aerogear.unifiedpush.message.InternalUnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.message.NotificationRouter;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.util.HttpRequestUtil;
import org.jboss.aerogear.unifiedpush.rest.util.PushAppAuthHelper;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qmino.miredot.annotations.ReturnType;

@Path("/applicationsData")
public class PushApplicationDataEndpoint extends AbstractBaseEndpoint {
	private final Logger logger = LoggerFactory.getLogger(PushApplicationDataEndpoint.class);

	@Inject
	private PushApplicationService pushAppService;

	@Inject
	private DocumentService documentService;

	@Inject
	private NotificationRouter notificationRouter;

	@Inject
	private AliasService aliasService;

	/**
	 * Overwrites existing aliases and properties of the push application with
	 * the given data. Deprecated since 1.1.4 - moved to alias endpoint.
	 *
	 * @param aliasData
	 *            list of aliases
	 * @param request
	 * @return
	 */
	@POST
	@Path("/aliases")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	@Deprecated
	public Response updateAliases(List<String> aliasData, @Context HttpServletRequest request) {
		return aliasesUpdate(aliasData, request);
	}

	/**
	 * Deprecated since 1.1.4 - moved to alias endpoint.
	 *
	 * @param pushApplicationID
	 * @param aliasData
	 * @param request
	 * @return
	 */
	@POST
	@Path("/{pushAppID}/aliases")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	@Deprecated
	public Response updateAliases(@PathParam("pushAppID") String pushApplicationID, List<String> aliasData,
			@Context HttpServletRequest request) {

		logger.error("method call to @deprecated API /applicationsData/{pushAppID}/aliases");

		return aliasesUpdate(aliasData, request);
	}

	private Response aliasesUpdate(List<String> aliasData, @Context HttpServletRequest request) {
		final PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request,
				pushAppService);
		if (pushApplication == null) {
			return Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
					.entity("Unauthorized Request").build();
		}

		try {
			// TODO - change oauth2 flag to false
			aliasService.updateAliasesAndInstallations(pushApplication, aliasData, true);
			return Response.ok(EmptyJSON.STRING).build();
		} catch (Exception e) {
			logger.error("Cannot update aliases", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("/document/{qualifier}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("multipart/form-data")
	@PartType(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response retrieveDocumentsForPushApp(@PathParam("qualifier") String qualifer, @PathParam("id") String id,
			@Context HttpServletRequest request) {
		return getDocumentsForPushApp(qualifer, id, request);
	}

	@GET
	@Path("/{pushAppID}/document/{qualifier}/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("multipart/form-data")
	@PartType(MediaType.TEXT_PLAIN)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	@Deprecated
	public Response retrieveDocumentsForPushApp(@PathParam("pushAppID") String pushApplicationID,
			@PathParam("qualifier") String qualifer, @PathParam("id") String id, @Context HttpServletRequest request) {

		logger.error("method call to @deprecated API /applicationsData/{pushAppID}/document/{qualifier}/{id}");

		return getDocumentsForPushApp(qualifer, id, request);
	}

	public Response getDocumentsForPushApp(@PathParam("qualifier") String qualifer, @PathParam("id") String id,
			@Context HttpServletRequest request) {
		final PushApplication pushApp = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request, pushAppService);

		if (pushApp == null) {
			return Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
					.entity("Unauthorized Request").build();
		}

		try {
			MultipartFormDataOutput mdo = new MultipartFormDataOutput();
			List<String> documents = documentService.getLatestDocumentsForApplication(pushApp, qualifer, id);
			for (int i = 0; i < documents.size(); i++) {
				mdo.addFormData("file" + i, documents.get(i), MediaType.TEXT_PLAIN_TYPE);
			}

			logger.debug(String.format("%s documents found for push applicaiton %s",
					documents != null ? documents.size() : 0, pushApp.getPushApplicationID()));

			return Response.ok(mdo).build();
		} catch (Exception e) {
			logger.error("Cannot retrieve documents for push app", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}


	/**
	 * POST deploys a file and stores it for later retrieval by a client of the
	 * push application. Deprecated since 1.1.3 - moved to document endpoint
	 *
	 * @deprecated Use @Path("/sender/payload/")
	 * @param pushApplicationID
	 *            id of
	 *            {@link org.jboss.aerogear.unifiedpush.api.PushApplication}
	 * @param deployRequest
	 *            a map between aliases and documents.
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
			DocumentDeployRequest deployRequest, @Context HttpServletRequest request,
			@DefaultValue("false") @QueryParam("overwrite") boolean overwrite) {

		logger.warn("method call to @deprecated API /applicationsData/{pushAppID}/document");

		final PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request,
				pushAppService);

		if (pushApplication == null) {
			return Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
					.entity("Unauthorized Request").build();
		}

		if (!deployRequest.getAliasToDocument().isEmpty()) {
			try {
				documentService.saveForAliases(pushApplication, deployRequest.getAliasToDocument(),
						DocumentMetadata.getQualifier(deployRequest.getQualifier()), null, overwrite);

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
				logger.error("Cannot deploy file for alias", e);
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}

		return Response.ok(EmptyJSON.STRING).build();
	}
}
