package org.jboss.aerogear.unifiedpush.rest.documents;

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

import org.apache.commons.lang.StringUtils;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.annotations.PATCH;
import org.jboss.aerogear.unifiedpush.rest.util.ClientAuthHelper;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;

import com.qmino.miredot.annotations.ReturnType;

@Path("/document")
public class DocumentEndpoint {
    private final AeroGearLogger logger = AeroGearLogger.getInstance(DocumentEndpoint.class);

	@Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private GenericVariantService genericVariantService;
    @Inject
    private DocumentService documentService;
    @Inject
    private PushApplicationService pushApplicationService;

    /**
     * Cross Origin for Installations
     *
     * @param headers   "Origin" header
     * @return          "Access-Control-Allow-Origin" header for your response
     *
     * @responseheader Access-Control-Allow-Origin      With host in your "Origin" header
     * @responseheader Access-Control-Allow-Methods     POST, DELETE
     * @responseheader Access-Control-Allow-Headers     accept, origin, content-type, authorization
     * @responseheader Access-Control-Allow-Credentials true
     * @responseheader Access-Control-Max-Age           604800
     *
     * @statuscode 200 Successful response for your request
     */
    @OPTIONS
    @ReturnType("java.lang.Void")
    public Response crossOriginForInstallations(@Context HttpHeaders headers) {
        return appendPreflightResponseHeaders(headers, Response.ok()).build();
    }

    private ResponseBuilder appendPreflightResponseHeaders(HttpHeaders headers, ResponseBuilder response) {
        // add response headers for the preflight request
        // required
        response.header("Access-Control-Allow-Origin", headers.getRequestHeader("Origin").get(0)) // return submitted origin
                .header("Access-Control-Allow-Methods", "POST, DELETE, PATCH") // only POST/DELETE/PATCH are allowed
                .header("Access-Control-Allow-Headers", "accept, origin, content-type, authorization") // explicit Headers!
                .header("Access-Control-Allow-Credentials", "true")
                // indicates how long the results of a preflight request can be cached (in seconds)
                .header("Access-Control-Max-Age", "604800"); // for now, we keep it for seven days

        return response;
    }

    /**
     * POST deploys a file and stores it for later retrieval by the push application
     * of the client.
     */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{publisher}/{alias}/{qualifier}{id : (/[^/]+?)?}")
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response newDocument(String entity, @PathParam("publisher") String publisher,
			@PathParam("alias") String alias, @PathParam("qualifier") String qualifier,
			@PathParam("id") String id,
			@Deprecated @DefaultValue("false") @QueryParam("overwrite") boolean overwrite,
			@Context HttpServletRequest request) {

		// Overwrite is @Deprecated, this method should always use overwrite false - will be removed on 1.3.0

		// Store new document according to path params.
		// If document exists a newer version will be stored.
		return deployDocument(entity, publisher, alias, qualifier, id, overwrite, request);
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{publisher}/{alias}/{qualifier}{id : (/[^/]+?)?}")
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response updateDocument(String entity, @PathParam("publisher") String publisher,
			@PathParam("alias") String alias, @PathParam("qualifier") String qualifier,
			@PathParam("id") String id,
			@Context HttpServletRequest request) {

		// Store new document according to path params.
		// If document exists update stored version.
		return deployDocument(entity, publisher, alias, qualifier, id, true, request);
	}

	private Response deployDocument(String entity, String publisher,String alias, String qualifier,
			String id, boolean overwrite, HttpServletRequest request) {

		final Variant variant = ClientAuthHelper.loadVariantWhenInstalled(genericVariantService,
				clientInstallationService, request);
		if (variant == null) {
			return create401Response(request);
		}

		if (StringUtils.isEmpty(id)) {
			id = DocumentMessage.NULL_PART;
		} else {
			id = id.substring(1); // remove first '/'
		}

		try {
			PushApplication pushApp = pushApplicationService.findByVariantID(variant.getVariantID());
			documentService.saveForPushApplication(pushApp, alias, entity,
					DocumentMetadata.getQualifier(qualifier), id, overwrite);
			return Response.ok(EmptyJSON.STRING).build();
		} catch (Exception e) {
			logger.severe("Cannot deploy file for push application", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}


    private Response appendAllowOriginHeader(ResponseBuilder rb, HttpServletRequest request) {

        return rb.header("Access-Control-Allow-Origin", request.getHeader("Origin")) // return submitted origin
                .header("Access-Control-Allow-Credentials", "true").type(MediaType.APPLICATION_JSON)
                 .build();
    }

    private Response create401Response(final HttpServletRequest request) {
        return appendAllowOriginHeader(
                Response.status(Status.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                        .entity("Unauthorized Request"),
                request);
    }

	/**
	 * Get latest (last-updated) document according to path parameters </br>
	 * <b>Examples:</b></br>
	 * <li>document/application/17327572923/test/latest - alias specific document
	 * <li>document/application/null/test/latest - global scope document (for any alias).
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{publisher}/{alias}/{qualifier}/latest")
	public Response retrieveDocument(@PathParam("publisher") String publisher, @PathParam("alias") String alias,
			@PathParam("qualifier") String qualifier, @Context HttpServletRequest request) {
		final Variant variant = ClientAuthHelper.loadVariantWhenInstalled(genericVariantService,
				clientInstallationService, request);
		if (variant == null) {
			return create401Response(request);
		}

		try {
			String document = documentService.getLatestDocumentForAlias(variant, DocumentMetadata.getPublisher(publisher), alias, DocumentMetadata.getQualifier(qualifier));
			return Response.ok(StringUtils.isEmpty(document) ? EmptyJSON.STRING: document).build();
		} catch (Exception e) {
			logger.severe("Cannot retrieve files for alias", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
