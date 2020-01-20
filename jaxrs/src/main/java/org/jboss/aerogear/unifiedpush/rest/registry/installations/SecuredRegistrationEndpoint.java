package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import java.util.Collection;
import java.util.Collections;

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
import org.jboss.aerogear.unifiedpush.rest.util.BearerHelper;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.impl.PushApplicationServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.UserTenantInfo;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	/**
	 * Endpoint for adding UTR to an existing user at Keycloak.
	 * Notice: after successful UTR sync user credentials shall be disabled
	 *
	 * @param request The request object
	 *
	 * return UTR
	 *
	 * @statuscode 200 Successful storage of the alias.
	 * @statuscode 401 if access token not found
	 * @statuscode 404 if keyclaok user or UTR for the user not found
	 */
	@POST
	@Path("/bindWithSSO")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response bindWithSSO(@Context HttpServletRequest request) {
		AccessToken accessToken = BearerHelper.getTokenDataFromBearer(request).orNull();
		if (accessToken == null) {
			return create401Response(request);
		}

		String alias = accessToken.getPreferredUsername();

		PushApplication app = authenticationHelper.loadApplicationWhenAuthorized(request);
		if (!keycloakService.exists(alias, app.getName())) {
			return Response.status(Response.Status.NOT_FOUND.getStatusCode(), "Keycloak user " + alias + " not found").build();
		}

		Collection<UserTenantInfo> tenantRelations = aliasService.getTenantRelations(alias);
		if (tenantRelations.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND.getStatusCode(), "UTR for user " + alias + " not found").build();
		}
		keycloakService.updateTenantsExistingUser(alias, tenantRelations, app.getName());

		String realmName = keycloakService.getRealmName(app.getName());

		// add 'installation' realm role
		keycloakService.addUserRealmRoles(Collections.singletonList(keycloakService.KEYCLOAK_ROLE_USER), alias, realmName);

		// disable password
		keycloakService.disableUserCredentials(alias, realmName);

		return Response.ok(tenantRelations).build();
	}
}
