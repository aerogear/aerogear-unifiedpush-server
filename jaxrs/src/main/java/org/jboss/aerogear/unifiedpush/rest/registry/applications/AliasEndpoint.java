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
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.PasswordContainer;
import org.jboss.aerogear.unifiedpush.rest.util.BearerHelper;
import org.jboss.aerogear.unifiedpush.rest.util.PushAppAuthHelper;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qmino.miredot.annotations.ReturnType;

@Path("/alias")
public class AliasEndpoint extends AbstractBaseEndpoint {
	private final Logger logger = LoggerFactory.getLogger(AliasEndpoint.class);

	@Inject
	private PushApplicationService pushAppService;

	@Inject
	private AliasService aliasService;

	/**
	 * Cross Origin for Alias
	 *
	 * @param headers
	 *            "Origin" header
	 * @return "Access-Control-Allow-Origin" header for your response
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Methods POST, DELETE
	 * @responseheader Access-Control-Allow-Headers accept, origin,
	 *                 content-type, authorization
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader Access-Control-Max-Age 604800
	 *
	 * @statuscode 200 Successful response for your request
	 */
	@OPTIONS
	@Path("/{aliasId}")
	@ReturnType("java.lang.Void")
	public Response crossOriginForAlias(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}
	
	@GET
	@Path("/exists/{aliasId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	/**
	 * Public API to validate alias doesn't exists
	 */
	public Response exists(@PathParam("aliasId") String aliasId, @Context HttpServletRequest request) {
		if (aliasService.find(aliasId) != null)
			return appendAllowOriginHeader(Response.ok().entity(Boolean.TRUE), request);

		return appendAllowOriginHeader(Response.ok().entity(Boolean.FALSE), request);
	}

	/**
	 * RESTful API for updating alias password. The Endpoint is protected using
	 * <code>HTTP Basic</code> (credentials
	 * <code>ApplicationID:master secret</code>).
	 *
	 * @param aliasId
	 * @param passwordContainer
	 * @return registered {@link Installation}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="UnifiedPush Server" (only
	 *                 for 401 response)
	 *
	 * @statuscode 200 Successful storage of the aliases.
	 * @statuscode 400 The format of the aliases request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	// TODO - Find a way to validate current user password.
	@POST
	@Path("/{aliasId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response setAliasPassword(@PathParam("aliasId") String aliasId, PasswordContainer passwordContainer,
			@Context HttpServletRequest request) {
		try {
			AccessToken accessToken = BearerHelper.getTokenDataFromBearer(request);
			if (accessToken != null && accessToken.getPreferredUsername().equals(aliasId)) {
				ResponseBuilder response = Response.notModified();
				if (passwordContainer.isDataValid()) {
					aliasService.updateAliasePassword(aliasId, passwordContainer.getCurrentPassword(),
							passwordContainer.getNewPassword());

					response = Response.ok(EmptyJSON.STRING);
				}

				return appendAllowOriginHeader(response, request);
			}

			return create401Response(request);
		} catch (Exception e) {
			logger.error("Cannot update aliases", e);
			return appendAllowOriginHeader(Response.status(Status.INTERNAL_SERVER_ERROR), request);
		}
	}

	/***********************************************************************
	 * Admin API Section, must be protected with application id & password *
	 ***********************************************************************/
	/**
	 * RESTful API for synchronize aliases of the push application. The Endpoint
	 * is protected using <code>HTTP Basic</code> (credentials
	 * <code>ApplicationID:master secret</code>).
	 *
	 * <pre>
	 * curl -u "ApplicationID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json" -H "aerogear-push-id: someid"
	 *   -X POST
	 *   -d '{
	 *     ["123456789", "test@ups.com"]
	 *   }'
	 *   https://SERVER:PORT/context/rest/alias/aliases?oauth2=true
	 * </pre>
	 *
	 * Details about JSON format can be found HERE!
	 *
	 * @param aliasData
	 *            List of aliases related to push application
	 * @return registered {@link Installation}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="UnifiedPush Server" (only
	 *                 for 401 response)
	 *
	 * @statuscode 200 Successful storage of the aliases.
	 * @statuscode 400 The format of the aliases request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@POST
	@Path("/aliases")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response updateAliases(List<String> aliasData, @QueryParam("oauth2") @DefaultValue("false") boolean oauth2,
			@Context HttpServletRequest request) {
		final PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request,
				pushAppService);
		if (pushApplication == null) {
			return Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
					.entity("Unauthorized Request").build();
		}

		try {
			aliasService.updateAliasesAndInstallations(pushApplication, aliasData, oauth2);
			return Response.ok(EmptyJSON.STRING).build();
		} catch (Exception e) {
			logger.error("Cannot update aliases", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}