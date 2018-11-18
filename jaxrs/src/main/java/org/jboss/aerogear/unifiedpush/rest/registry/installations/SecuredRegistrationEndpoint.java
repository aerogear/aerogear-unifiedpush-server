package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.VariantType;
import org.jboss.aerogear.unifiedpush.service.impl.PushApplicationServiceImpl;
import org.springframework.stereotype.Controller;

import com.qmino.miredot.annotations.ReturnType;

@Controller
@Path("/registry/type")
public class SecuredRegistrationEndpoint extends AbstractBaseRegistrationEndpoint {

	@POST
	@Path("{type}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.api.Installation")
	public Response registerSecured(@DefaultValue("") @HeaderParam("x-ag-old-token") final String oldToken,
			Installation entity, @DefaultValue("false") @QueryParam("synchronously") boolean synchronously,
			@PathParam("type") VariantType type, @Context HttpServletRequest request) {

		PushApplication app = authenticationHelper.loadApplicationWhenAuthorized(request);
		if (app == null) {
			return create401Response(request);
		}

		Variant var = PushApplicationServiceImpl.getByVariantType(app, type).orElse(null);
		if (var == null) {
			return create401Response(request);
		}

		return register(var, oldToken, entity, synchronously, request);
	}

}
