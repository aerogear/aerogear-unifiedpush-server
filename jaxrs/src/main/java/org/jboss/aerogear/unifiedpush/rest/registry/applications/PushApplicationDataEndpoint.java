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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.PushAppAuthHelper;
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

	private Response getDocumentsForPushApp(String qualifer, String id, HttpServletRequest request) {
		final PushApplication pushApp = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request, pushAppService);

		if (pushApp == null) {
			return Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", "Basic realm=\"AeroBase UnifiedPush Server\"")
					.entity("Unauthorized Request").build();
		}

		try {
			MultipartFormDataOutput mdo = new MultipartFormDataOutput();
			List<String> documents = documentService.getLatestFromAliases(pushApp, qualifer, id);
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
}
