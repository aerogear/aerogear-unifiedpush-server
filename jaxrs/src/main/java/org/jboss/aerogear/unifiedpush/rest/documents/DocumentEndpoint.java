package org.jboss.aerogear.unifiedpush.rest.documents;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.Installation;
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
    @ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
    public Response deployDocumentsForPushApp(String entity, @QueryParam("type") String type, @Context HttpServletRequest request) {
        
		final Variant variant = ClientAuthHelper.loadVariantWhenInstalled(genericVariantService, clientInstallationService, request);
		if (variant == null) {
			return getUnauthorizedResponse();
		}
        
        try {
        	documentService.saveForPushApplication(ClientAuthHelper.getDeviceToken(request), variant, entity, type);
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
	
	@GET	
	@Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
    public Response retrieveDocumentsForAlias(@QueryParam("date") Long date, 
    		@QueryParam("type") String type, @Context HttpServletRequest request) {
		final Variant variant = ClientAuthHelper.loadVariantWhenInstalled(genericVariantService, clientInstallationService, request);
		if (variant == null) {
			return getUnauthorizedResponse();
		}
		Installation installation = clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), 
				ClientAuthHelper.getDeviceToken(request));
		
        try {
        	List<String> documents = documentService.getAliasDocuments(variant, installation.getAlias(), type, new Date(date));
        	return Response.ok(documents).build();
        } catch (Exception e) {
        	logger.severe("Cannot retrieve files for alias", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }   
	
}
