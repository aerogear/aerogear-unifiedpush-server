package org.jboss.aerogear.unifiedpush.rest.documents;

import java.util.Arrays;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.parser.JsonDocumentContent;
import org.jboss.aerogear.unifiedpush.rest.AbstractEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.rest.util.PushAppAuthHelper;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qmino.miredot.annotations.ReturnType;

@Path("/database")
public class DatabaseApplicationEndpoint extends AbstractEndpoint {
	private final Logger logger = LoggerFactory.getLogger(DatabaseApplicationEndpoint.class);

	@Inject
	private PushApplicationService pushApplicationService;
	@Inject
	private DocumentService documentService;
	@Inject
	private AliasService aliasService;

	/**
	 * RESTful API for querying aliases documents. The Endpoint is protected
	 * using <code>HTTP Basic</code> (credentials
	 * <code>ApplicationID:Master Secret</code>).
	 *
	 * <pre>
	 * curl -u "ApplicationID:Master Secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X POST
	 *   -d '["GUID", "GUID"]'
	 *   "https://SERVER:PORT/context/rest/database/DEVICES/aliases"
	 * </pre>
	 *
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 * @param uuids
	 *            User time-based UUIDs
	 *
	 * @return {@link java.lang.Void}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroBase Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 200 Successful query of documents.
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	@Path("/{database}/aliases")
	public Response getForAlias(@Context HttpServletRequest request, //
			@QueryParam("id") String id, //
			@QueryParam("fromDate") Long fromDate, //
			@QueryParam("toDate") Long toDate, //
			@QueryParam("limit") Integer limit, //
			@HeaderParam("Accept") String accept, //
			@PathParam("database") String database, //
			String[] uuids) {

		// Try application based authentication.
		PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request,
				pushApplicationService);

		if (pushApplication == null) {
			logger.warn("UnAuthorized application authentication attempt, credentials ({}) are not authorized",
					HttpBasicHelper.getAuthorizationHeader(request));
			return create401Response(request);
		}

		// Input validation
		if (ArrayUtils.isEmpty(uuids)) {
			return appendAllowOriginHeader(Response.status(Status.BAD_REQUEST), request);
		}

		final DocumentList docs = new DocumentList();
		UUID pushApplicationId = UUID.fromString(pushApplication.getPushApplicationID());

		Arrays.asList(uuids).forEach(userId -> {
			// Validate userId exists with application scope
			collect(request, docs, new QueryOptions(fromDate, toDate, id, limit), pushApplicationId, database, userId);
		});

		return appendAllowOriginHeader(
				DatabaseEndpoint.appendCountHeader(Response.ok(docs), docs.getDocuments().size()), request);
	}

	private void collect(HttpServletRequest request, DocumentList docs, //
			QueryOptions options, //
			UUID pushApplicationId, //
			String database, //
			String userId) { //

		try {
			UUID uuid = UUID.fromString(userId);

			if (aliasService.find(pushApplicationId, uuid) != null) {
				DocumentMetadata metadata = new DocumentMetadata(pushApplicationId, database, uuid);
				documentService.find(metadata, options).forEach(doc -> {
					docs.getDocuments()
							.add(new JsonDocumentContent(doc.getKey(), doc.getContent(), doc.getDocumentId()));
				});
			} else {
				logger.debug("UserId {} was not found in application {} scope", userId, pushApplicationId);
				docs.ignore(uuid.toString());
			}
		} catch (IllegalArgumentException e) {
			logger.debug("Unable to parse UUID {}", userId);
			docs.ignore(userId);
		}
	}
}
