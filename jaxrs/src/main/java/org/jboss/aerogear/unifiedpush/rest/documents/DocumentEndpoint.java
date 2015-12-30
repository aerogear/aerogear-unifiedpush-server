package org.jboss.aerogear.unifiedpush.rest.documents;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.jboss.aerogear.unifiedpush.api.DocumentMessage;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.util.ClientAuthHelper;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
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
    
    @OPTIONS
    @ReturnType("java.lang.Void")
    public Response crossOrigin(
            @Context HttpHeaders headers,
            @PathParam("token") String token) {

    	return Response.ok().header("Access-Control-Allow-Origin", headers.getRequestHeader("Origin").get(0)) // return submitted origin
                .header("Access-Control-Allow-Methods", "POST, GET") // only POST/DELETE are allowed
                .header("Access-Control-Allow-Headers", "accept, origin, content-type, authorization") // explicit Headers!
                .header("Access-Control-Allow-Credentials", "true")
                // indicates how long the results of a preflight request can be cached (in seconds)
                .header("Access-Control-Max-Age", "604800") // for now, we keep it for seven days
                .build();
    }
	
    /**
     * POST deploys a file and stores it for later retrieval by the push application
     * of the client.
     */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{publisher}/{alias}/{qualifier}")
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response deployDocuments(String entity, @PathParam("publisher") String publisher,
			@PathParam("alias") String alias, @PathParam("qualifier") String qualifier,
			@Context HttpServletRequest request) {

		final Variant variant = ClientAuthHelper.loadVariantWhenInstalled(genericVariantService,
				clientInstallationService, request);
		if (variant == null) {
			return getUnauthorizedResponse();
		}

		try {
			documentService.saveForPushApplication(ClientAuthHelper.getDeviceToken(request), variant, entity,
					DocumentMessage.getQualifier(qualifier));
			return Response.ok(EmptyJSON.STRING).build();
		} catch (Exception e) {
			logger.severe("Cannot deploy file for push application", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	private Response getUnauthorizedResponse() {
		return Response.status(Status.UNAUTHORIZED)
	            .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
	            .entity("Unauthorized Request").build();
	}

	/**
	 * Get latest (last-updated) document according to path parameters </br>
	 * <b>Examples:</b></br>
	 * <li>document/application/17327572923/test/latest - alias specific document 
	 * <li>document/application/null/test/latest - global scope document (for any alias).
	 */
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/{publisher}/{alias}/{qualifier}/latest")
	public Response retrieveDocument(@PathParam("publisher") String publisher, @PathParam("alias") String alias,
			@PathParam("qualifier") String qualifier, @Context HttpServletRequest request) {
		final Variant variant = ClientAuthHelper.loadVariantWhenInstalled(genericVariantService,
				clientInstallationService, request);
		if (variant == null) {
			return getUnauthorizedResponse();
		}

		try {
			String document = documentService.getLatestDocument(variant, DocumentMessage.getPublisher(publisher), alias, DocumentMessage.getQualifier(qualifier));
			return Response.ok(StringUtils.isEmpty(document) ? EmptyJSON.STRING: document).build();
		} catch (Exception e) {
			logger.severe("Cannot retrieve files for alias", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

}
