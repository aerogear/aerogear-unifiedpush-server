package org.jboss.aerogear.unifiedpush.rest.deploy;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.PushMessageInformation;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.registry.installations.ImporterForm;
import org.jboss.aerogear.unifiedpush.rest.util.PushAppAuthHelper;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.file.AliasFileService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import com.qmino.miredot.annotations.ReturnType;

@Path("/deploy")
public class DeployEndpoint {
	
    private final AeroGearLogger logger = AeroGearLogger.getInstance(DeployEndpoint.class);
	
	@Inject
	private AliasFileService fileService;
	
	@Inject
	private PushApplicationService pushApplicationService;
	
	/**
     * POST deploys a file and stores it for later retrieval by a client 
     * of the push application.
     *
     * @param pushAppId id of {@link org.jboss.aerogear.unifiedpush.api.PushApplication}
     * @param alias     the alias of the client
     * @param fileName  name of file to save
     *
     * @statuscode 401 if unauthorized for this push application
     * @statuscode 500 if request failed
     * @statuscode 200 upon success
     */
	@POST	
	@Path("/application/{pushAppID}/alias/{alias}/doc/{fileName}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
    public Response deployDocumentsForAlias(@PathParam("pushAppID") String pushApplicationID, 
    		@PathParam("alias") String alias, @PathParam("fileName") String fileName, 
    		@MultipartForm ImporterForm form, @Context HttpServletRequest request) {
        final PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request, pushApplicationService);
        if (pushApplication == null) {
            return Response.status(Status.UNAUTHORIZED)	
                    .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                    .entity("Unauthorized Request")
                    .build();
        }
        
        try {
        	fileService.writeForAlias(pushApplication, alias, fileName, form.getJsonFile());
        	return Response.ok(EmptyJSON.STRING).build();
        } catch (Exception e) {
        	logger.severe("Cannot deploy file", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
}
