package org.jboss.aerogear.unifiedpush.rest.documents;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.IDocument;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullAlias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.parser.JsonDocumentContent;
import org.jboss.aerogear.unifiedpush.rest.AbstractEndpoint;
import org.jboss.aerogear.unifiedpush.rest.authentication.AuthenticationHelper;
import org.jboss.aerogear.unifiedpush.rest.util.ClientAuthHelper;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.qmino.miredot.annotations.ReturnType;

@Controller
@Path("/database")
public class DatabaseEndpoint extends AbstractEndpoint {
	private final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

	public static final String X_HEADER_SNAPSHOT_ID = "X-AB-Snapshot-Id";
	public static final String X_HEADER_COUNT = "X-AB-Count";
	// Date header is added by nginx/wildfly and accessed by clients.
	private static final String HEADER_DATE = "Date";

	@Inject
	private DocumentService documentService;
	@Inject
	private AliasService aliasService;
	@Inject
	private AuthenticationHelper authenticationHelper;

	/**
	 * Cross Origin for application scope database.
	 *
	 * @param headers
	 *            "Origin" header
	 * @return "Access-Control-Allow-Origin" header for your response
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Methods POST, PUT, DELETE, HEAD
	 * @responseheader Access-Control-Allow-Headers accept, origin,
	 *                 content-type, authorization
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader Access-Control-Max-Age 604800
	 *
	 * @statuscode 200 Successful response for your request
	 */
	@OPTIONS
	@Path("/{database}")
	@ReturnType("java.lang.Void")
	public Response crossOriginForApplication(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	/**
	 * Cross Origin for application scope database.
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
	@Path("/{database}/{snapshot}")
	@ReturnType("java.lang.Void")
	public Response crossOriginForApplicationPut(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	/**
	 * Cross Origin for application scope database.
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
	@Path("/{database}/alias/{alias}")
	@ReturnType("java.lang.Void")
	public Response crossOriginForAlias(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	/**
	 * Cross Origin for application scope database.
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
	@Path("/{database}/alias/{alias}/{snapshot}")
	@ReturnType("java.lang.Void")
	public Response crossOriginForAliasPut(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	/**
	 * RESTful API for storing application scope document. The Endpoint is
	 * protected using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X POST
	 *   -d '{
	 *     "any-attribute-1" : "example1",
	 *     "any-attribute-2" : "example1",
	 *     "any-attribute-3" : "example1"
	 *   }'
	 *   "https://SERVER:PORT/context/rest/database/users"
	 * </pre>
	 *
	 * @param document
	 *            JSON content
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 * @param id
	 *            Document collection id.
	 *
	 * @return {@link java.lang.Void}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 204 Successful store of the document.
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	@Path("/{database}")
	public Response saveForApplication(String document, //
			@PathParam("database") String database, //
			@QueryParam("id") String id, //
			@Context HttpServletRequest request) { //

		return saveForApplication(document, database, null, id, request);
	}

	/**
	 * RESTful API for storing application scope document. The Endpoint is
	 * protected using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: multipart/form-data" -H "Content-type: multipart/form-data; boundary=115bbbac-ac64-403f-ac6e-fd9874c056ee"
	 *   -X POST
	 *   -d '{
	 *     "content":{YOUR JSON CONTENT},
	 *     "contentType":"application/json",
	 *     "documentId":"ANY ID"}'
	 *   }
	 *   "https://SERVER:PORT/context/rest/database/DEVICES"
	 * </pre>
	 *
	 * @param documents
	 *            List of {@link IDocument}
	 *
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 *
	 * @return {@link java.lang.Void}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 204 Successful store of the document.
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ReturnType("java.lang.Void")
	@Path("/{database}")
	public Response saveForApplication(List<JsonDocumentContent> documents, //
			@PathParam("database") String database, //
			@Context HttpServletRequest request) { //

		documents.forEach(document -> {
			saveForApplication(document.getContent(), database, null, document.getDocumentId(), request);
		});

		return appendAllowOriginHeader(Response.noContent(), request);
	}

	/**
	 * RESTful API for updating application scope document. The Endpoint is
	 * protected using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X PUT
	 *   -d '{
	 *     "any-attribute-1" : "example1",
	 *     "any-attribute-2" : "example1",
	 *     "any-attribute-3" : "example1"
	 *   }'
	 *   "https://SERVER:PORT/context/rest/database/users/snapshot"
	 * </pre>
	 *
	 * @param document
	 *            JSON content
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 * @param snapshot
	 *            Unique snapshot identifier (TimeBased UUID).
	 * @param id
	 *            Document collection identifier.
	 *
	 * @return {@link java.lang.Void}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 204 Successful update of the document.
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	@Path("/{database}/{snapshot}")
	public Response saveForApplication(String document, //
			@PathParam("database") String database, //
			@PathParam("snapshot") String snapshot, //
			@QueryParam("id") String id, //
			@Context HttpServletRequest request) { //

		// Authentication verification
		final PushApplication pushApplication = authenticationHelper.loadApplicationWhenAuthorized(request, null);

		if (pushApplication == null) {
			return create401Response(request);
		}

		UUID pushApplicationId = UUID.fromString(pushApplication.getPushApplicationID());

		// Create metadata object
		DocumentMetadata metadata = new DocumentMetadata(pushApplicationId, database,
				NullAlias.getAlias(pushApplicationId),
				StringUtils.isEmpty(snapshot) ? null : UUID.fromString(snapshot));

		DocumentContent doc = documentService.save(metadata, document, id);

		try {
			return appendAllowOriginHeader(appendSnapshotHeader(Response.noContent(), doc.getKey().getSnapshot()),
					request);
		} catch (Exception e) {
			logger.error(String.format("Cannot store document for database %s", database), e);
			return appendAllowOriginHeader(Response.status(Status.INTERNAL_SERVER_ERROR), request);
		}
	}

	/**
	 * RESTful API for update alias scope document. The Endpoint is protected
	 * using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X POST
	 *   -d '{
	 *     "any-attribute-1" : "example1",
	 *     "any-attribute-2" : "example1",
	 *     "any-attribute-3" : "example1"
	 *   }'
	 *   "https://SERVER:PORT/context/rest/database/users/alias/support@aerogear.orgt"
	 * </pre>
	 *
	 * @param document
	 *            JSON content.
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 * @param alias
	 *            Unique alias name (email/phone/tokenid/other).
	 * @param id
	 *            Document collection id.
	 *
	 * @return {@link java.lang.Void}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 204 Successful update of the document.
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	@Path("/{database}/alias/{alias}")
	public Response saveForAlias(String document, //
			@PathParam("database") String database, //
			@PathParam("alias") String alias, //
			@QueryParam("id") String id, //
			@Context HttpServletRequest request) { //
		return saveForAlias(document, database, alias, null, id, request);
	}

	/**
	 * RESTful API for storing alias scope document. The Endpoint is protected
	 * using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: multipart/form-data" -H "Content-type: multipart/form-data; boundary=115bbbac-ac64-403f-ac6e-fd9874c056ee"
	 *   -X POST
	 *   -d '{
	 *     "content":{YOUR JSON CONTENT},
	 *     "contentType":"application/json",
	 *     "documentId":"ANY ID"}'
	 *   }
	 *   "https://SERVER:PORT/context/rest/database/DEVICES/alias/support@aerogear.org"
	 * </pre>
	 *
	 * @param documents
	 *            List of {@link IDocument}
	 *
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 *
	 * @param alias
	 *            Unique alias name (email/phone/tokenid/other).
	 *
	 * @return {@link java.lang.Void}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 204 Successful store of the document.
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ReturnType("java.lang.Void")
	@Path("/{database}/alias/{alias}")
	public Response saveForAlias(List<JsonDocumentContent> documents, //
			@PathParam("database") String database, //
			@PathParam("alias") String alias, //
			@Context HttpServletRequest request) { //

		documents.forEach(document -> {
			saveForAlias(document.getContent(), database, alias, null, document.getDocumentId(), request);
		});

		return appendAllowOriginHeader(Response.noContent(), request);
	}

	/**
	 * RESTful API for update alias scope document. The Endpoint is protected
	 * using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X POST
	 *   -d '{
	 *     "any-attribute-1" : "example1",
	 *     "any-attribute-2" : "example1",
	 *     "any-attribute-3" : "example1"
	 *   }'
	 *   "https://SERVER:PORT/context/rest/database/users/alias/support@aerogear.org/snapshot"
	 * </pre>
	 *
	 * @param document
	 *            JSON content.
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 * @param alias
	 *            Unique alias name (email/phone/tokenid/other).
	 * @param snapshot
	 *            Unique snapshot identifier (TimeBased UUID).
	 * @param id
	 *            Document collection id.
	 *
	 * @return {@link java.lang.Void}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 204 Successful update of the document.
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	@Path("/{database}/alias/{alias}/{snapshot}")
	public Response saveForAlias(String document, //
			@PathParam("database") String database, //
			@PathParam("alias") String alias, //
			@PathParam("snapshot") String snapshot, //
			@QueryParam("id") String id, //
			@Context HttpServletRequest request) { //

		// Get device-token authentication
		final PushApplication pushApplication = authenticationHelper.loadApplicationWhenAuthorized(request, alias);

		if (pushApplication == null) {
			return create401Response(request);
		}

		// Find application by variant
		UUID pushApplicationId = UUID.fromString(pushApplication.getPushApplicationID());

		// Find related alias
		Alias aliasObj = aliasService.find(pushApplicationId.toString(), alias);

		if (aliasObj == null) {
			String deviceToken = ClientAuthHelper.getDeviceToken(request);
			logger.warn("Alias is missing. storing by token-id. appId={}, token={}, alias={}", pushApplicationId, deviceToken, alias);
			aliasObj = getAliasByToken(pushApplicationId, deviceToken);
		}

		DocumentMetadata metadata = new DocumentMetadata(pushApplicationId, database, aliasObj,
				StringUtils.isEmpty(snapshot) ? null : UUID.fromString(snapshot));
		DocumentContent doc = documentService.save(metadata, document, id);

		try {
			return appendAllowOriginHeader(appendSnapshotHeader(Response.noContent(), doc.getKey().getSnapshot()),
					request);
		} catch (Exception e) {
			logger.error(String.format("Cannot store document for database %s", database), e);
			return appendAllowOriginHeader(Response.status(Status.INTERNAL_SERVER_ERROR), request);
		}
	}

	/**
	 * RESTful API for query application scope document. The Endpoint is
	 * protected using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X HEAD
	 *   "https://SERVER:PORT/context/rest/database/metadata"
	 * </pre>
	 *
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 * @param id
	 *            Document collection id.
	 * @param fromDate
	 *            Limit query, return documents newer (>=) then fromDate.
	 * @param toDate
	 *            Limit query, return documents older (<) then toDate.
	 * @param limit
	 *            max number of documents.
	 *
	 * @return Headers Only.
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 204 The HEAD method is identical to GET except that the
	 *             server DOES NOT return a message-body in the response
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@HEAD
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	@Path("/{database}")
	public Response headForApplication(@PathParam("database") String database, //
			@QueryParam("id") String id, //
			@QueryParam("fromDate") Long fromDate, //
			@QueryParam("toDate") Long toDate, //
			@QueryParam("limit") Integer limit, //
			@HeaderParam("Accept") String accept, //
			@Context HttpServletRequest request) { //
		return get(request, new QueryOptions(fromDate, toDate, id, limit), accept, false, database, null);
	}

	/**
	 * RESTful API for query application scope document. The Endpoint is
	 * protected using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X GET
	 *   "https://SERVER:PORT/context/rest/database/metadata"
	 * </pre>
	 *
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 * @param id
	 *            Document collection id.
	 * @param fromDate
	 *            Limit query, return documents newer (>=) then fromDate.
	 * @param toDate
	 *            Limit query, return documents older (<) then toDate.
	 * @param limit
	 *            max number of documents.
	 *
	 * @return Document content as application/json.
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 200 Successful query of the documents.
	 * @statuscode 204 Successful query of the documents but no available
	 *             content.
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	@Path("/{database}")
	public Response getForApplication(@PathParam("database") String database, //
			@QueryParam("id") String id, //
			@QueryParam("fromDate") Long fromDate, //
			@QueryParam("toDate") Long toDate, //
			@QueryParam("limit") Integer limit, //
			@HeaderParam("Accept") String accept, //
			@Context HttpServletRequest request) { //
		return get(request, new QueryOptions(fromDate, toDate, id, limit), accept, false, database, null);
	}

	/**
	 * RESTful API for query alias scope document. The Endpoint is protected
	 * using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X HEAD
	 *   "https://SERVER:PORT/context/rest/database/users/alias/support@aerogear.org/"
	 * </pre>
	 *
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 * @param alias
	 *            Unique alias name (email/phone/tokenid/other).
	 * @param id
	 *            Document collection id.
	 * @param fromDate
	 *            Limit query, return documents newer (>=) then fromDate.
	 * @param toDate
	 *            Limit query, return documents older (<) then toDate.
	 * @param limit
	 *            max number of documents.
	 *
	 * @return Headers Only.
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 204 The HEAD method is identical to GET except that the
	 *             server DOES NOT return a message-body in the response
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@HEAD
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	@Path("/{database}/alias/{alias}")
	public Response headForAlias(@PathParam("database") String database, //
			@PathParam("alias") String alias, //
			@QueryParam("id") String id, //
			@QueryParam("fromDate") Long fromDate, //
			@QueryParam("toDate") Long toDate, //
			@QueryParam("limit") Integer limit, //
			@HeaderParam("Accept") String accept, //
			@Context HttpServletRequest request) { //
		return get(request, new QueryOptions(fromDate, toDate, id, limit), accept, true, database, alias);
	}

	/**
	 * RESTful API for query alias scope document. The Endpoint is protected
	 * using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X GET
	 *   https://SERVER:PORT/context/rest/database/users/alias/support@aerogear.org/
	 * </pre>
	 *
	 * @param database
	 *            Logical database name, e.g users | metadata | any other.
	 * @param alias
	 *            Unique alias name (email/phone/tokenid/other).
	 * @param id
	 *            Document collection id.
	 * @param fromDate
	 *            Limit query, return documents newer (>=) then fromDate.
	 * @param toDate
	 *            Limit query, return documents older (<) then toDate.
	 * @param limit
	 *            max number of documents.
	 *
	 * @return Document content as application/json.
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin"
	 *                 header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear Server" (only for
	 *                 401 response)
	 *
	 * @statuscode 200 Successful query of the documents.
	 * @statuscode 204 Successful query without available documents.
	 * @statuscode 400 The format of the document request was incorrect (e.g.
	 *             missing required values).
	 * @statuscode 401 The request requires authentication.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	@Path("/{database}/alias/{alias}")
	public Response getForAlias(@PathParam("database") String database, //
			@PathParam("alias") String alias, //
			@QueryParam("id") String id, //
			@QueryParam("fromDate") Long fromDate, //
			@QueryParam("toDate") Long toDate, //
			@QueryParam("limit") Integer limit, //
			@HeaderParam("Accept") String accept, //
			@Context HttpServletRequest request) { //
		return get(request, new QueryOptions(fromDate, toDate, id, limit), accept, false, database, alias);
	}

	private Response get(HttpServletRequest request, //
			QueryOptions options, //
			String accept, //
			boolean headOnly, //
			String database, //
			String alias) {

		// Authentication validation
		PushApplication pushApplication = authenticationHelper.loadApplicationWhenAuthorized(request, alias);

		if (pushApplication == null) {
			return create401Response(request);
		}

		UUID pushApplicationId = UUID.fromString(pushApplication.getPushApplicationID());
		ResponseData data = getDocuments(request, options, accept, headOnly, pushApplicationId, database, alias);

		try {
			// In case no available parts | HEAD request, return 204
			if (headOnly || data.getSize() == 0)
				return appendAllowOriginHeader(appendCountHeader(Response.noContent(), data.getSize()), request);
			else
				return appendAllowOriginHeader(
						appendCountHeader(Response.ok(data.getResponse()).type(data.getContentType()), data.getSize()),
						request);
		} catch (Exception e) {
			logger.error(String.format("Cannot query documents for database %s, application %s", database,
					pushApplication.getPushApplicationID()), e);
			return appendAllowOriginHeader(Response.status(Status.INTERNAL_SERVER_ERROR), request);
		}
	}

	private ResponseData getDocuments(HttpServletRequest request, //
			QueryOptions options, //
			String accept, //
			boolean headOnly, //
			UUID pushApplicationId, //
			String database, //
			String alias) {

		// Find related alias by name
		Alias aliasObj;

		if (StringUtils.isNoneEmpty(alias)) {
			// Alias scope document
			aliasObj = aliasService.find(pushApplicationId.toString(), alias);

			// Alias scope document, but alias was not found.
			if (aliasObj == null) {
				String deviceToken = ClientAuthHelper.getDeviceToken(request);
				logger.warn("Alias is missing querying by token-id. appId={}, token={}, alias={}", pushApplicationId, deviceToken, alias);
				aliasObj = getAliasByToken(pushApplicationId, deviceToken);
			}
		} else {
			// application scope document
			aliasObj = NullAlias.getAlias(pushApplicationId);
		}

		return getAsApplicationJson(request, options, headOnly, pushApplicationId, database, aliasObj);
	}

	private ResponseData getAsApplicationJson(HttpServletRequest request, //
			QueryOptions options, //
			boolean headOnly, //
			UUID pushApplicationId, //
			String database, //
			Alias alias) { //

		final List<JsonDocumentContent> docs = new ArrayList<>();

		DocumentMetadata metadata = new DocumentMetadata(pushApplicationId, database, alias);
		documentService.find(metadata, options).forEach(doc -> {
			docs.add(new JsonDocumentContent(doc.getKey(), doc.getContent(), doc.getDocumentId()));
		});

		return new ResponseData(docs.size(), docs, MediaType.APPLICATION_JSON);
	}

	private Alias getAliasByToken(UUID pushApplicationId, String deviceToken) {
		// Find alias by device token
		Alias aliasObj = aliasService.find(pushApplicationId.toString(), deviceToken);

		if (aliasObj == null) {
			// Create alias by token id
			logger.debug("Alias {} is missing, creating new alias by token-id", deviceToken);
			aliasObj = new Alias(pushApplicationId, null, null, deviceToken);

			// Create anonymous alias
			aliasService.create(aliasObj);
		}

		return aliasObj;
	}

	public static ResponseBuilder appendCountHeader(ResponseBuilder rb, int count) {
		rb.header(X_HEADER_COUNT, count);

		return appendAllowExposeHeader(rb);
	}

	private ResponseBuilder appendSnapshotHeader(ResponseBuilder rb, UUID snapshot) {
		rb.header(X_HEADER_SNAPSHOT_ID, snapshot.toString());

		return appendAllowExposeHeader(rb);
	}

	public static ResponseBuilder appendAllowExposeHeader(ResponseBuilder rb) {
		rb.header("Access-Control-Expose-Headers",
				StringUtils.join(new String[] { X_HEADER_SNAPSHOT_ID, X_HEADER_COUNT, HEADER_DATE }, ","));
		return rb;
	}

	class ResponseData {
		private final int size;
		private final Object response;
		private final String contentType;

		public ResponseData(int size, Object response, String contentType) {
			super();
			this.size = size;
			this.response = response;
			this.contentType = contentType;
		}

		public int getSize() {
			return size;
		}

		public Object getResponse() {
			return response;
		}

		public String getContentType() {
			return contentType;
		}
	}
}
