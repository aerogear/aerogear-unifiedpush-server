package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import java.util.Collection;

import javax.inject.Inject;
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
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.impl.PushApplicationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.UserTenantInfo;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.qmino.miredot.annotations.ReturnType;

@Controller
@Path("/registry/type")
public class SecuredRegistrationEndpoint extends AbstractBaseRegistrationEndpoint {
	private final Logger logger = LoggerFactory.getLogger(SecuredRegistrationEndpoint.class);

	@Inject
	private AliasService aliasService;
	@Inject
	private IKeycloakService keycloakService;
	@Inject
	private PushApplicationService pushApplicationService;

	@POST
	@Path("/{type: .*}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.api.Installation")
	public Response registerSecured(@DefaultValue("") @HeaderParam("x-ag-old-token") final String oldToken,
			Installation entity, @DefaultValue("false") @QueryParam("synchronously") boolean synchronously,
			@PathParam("type") String type, @Context HttpServletRequest request) {

		PushApplication app = authenticationHelper.loadApplicationWhenAuthorized(request);

		if (app == null) {
			logger.debug("Unable to find application by jwt ticket");
			return create401Response(request);
		}

		Variant var = PushApplicationServiceImpl.getByVariantType(app, VariantType.getType(type)).orElse(null);
		if (var == null) {
			logger.debug("Unable to find variant " + type + " for application " + app.getName());
			return create401Response(request);
		}

		return register(var, oldToken, entity, synchronously, request);
	}

	@POST
	@Path("/syncUtr/{alias}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response syncUtr(@PathParam("alias") String alias, @Context HttpServletRequest request) {
		PushApplication app = authenticationHelper.loadApplicationWhenAuthorized(request);
		Collection<UserTenantInfo> tenantRelations = aliasService.getTenantRelations(alias);
		if (!keycloakService.exists(alias, app.getName())) {
			return Response.status(Response.Status.NOT_FOUND.getStatusCode(), "Keycloak user " + alias + " not found").build();
		}

		if (tenantRelations.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND.getStatusCode(), "UTR for user " + alias + " not found").build();
		}
		keycloakService.updateTenantsExistingUser(alias, tenantRelations, app.getName());
		return Response.ok(tenantRelations).build();
	}

	private AccessToken getAccessToken() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Object principal = authentication.getPrincipal();
		AccessToken token = null;
		if(principal instanceof KeycloakPrincipal) {
			KeycloakPrincipal<?> p = (KeycloakPrincipal<?>) principal;
			KeycloakSecurityContext keycloakSecurityContext = p.getKeycloakSecurityContext();
			token = keycloakSecurityContext.getToken();
		}
		return token;
	}

}
