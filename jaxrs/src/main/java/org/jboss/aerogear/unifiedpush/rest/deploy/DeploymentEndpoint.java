package org.jboss.aerogear.unifiedpush.rest.deploy;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.service.CategoryDeploymentService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

import com.qmino.miredot.annotations.ReturnType;

@Path("/deploy")
public class DeploymentEndpoint {
	
	@Inject
	private PushApplicationService pushApplicationService;
	
	@Inject
	private CategoryDeploymentService categoryDeploymentService;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	public Response deployCategories(Map<String, List<String>> categoryData, @Context HttpServletRequest request) {
		final PushApplication pushApplication = HttpBasicHelper.loadPushApplicationWhenAuthorized(pushApplicationService, request);
        if (pushApplication == null) {
            return HttpBasicHelper.createRequestIsUnauthorizedResponse();
        }
        
        categoryDeploymentService.deployCategories(pushApplication, categoryData);
        
        return Response.ok().build();
	}
}
