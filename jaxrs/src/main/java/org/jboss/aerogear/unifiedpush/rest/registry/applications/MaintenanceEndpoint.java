package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qmino.miredot.annotations.ReturnType;

@Path("/maintenance")
public class MaintenanceEndpoint {
	private final Logger logger = LoggerFactory.getLogger(MaintenanceEndpoint.class);

	@Inject
	private AliasService aliasService;
    @Inject
    private ClientInstallationService clientInstallationService;

	/**
	 * Remove alias from all applications
	 * @param aliasId
	 * @return
	 */

    @DELETE
   	@Path("/aliases/{aliasId}")
   	@Produces(MediaType.APPLICATION_JSON)
   	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
   	public Response removeAliad(@PathParam("aliasId") String aliasId) {

		try {
			aliasService.remove(null, aliasId);
			return Response.ok(EmptyJSON.STRING).build();
		} catch (Exception e) {
			logger.error("Cannot remove alias", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

    }

    @DELETE
   	@Path("/device/{aliasId}")
   	@Produces(MediaType.APPLICATION_JSON)
   	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
   	public Response removeDevice(@PathParam("aliasId") String aliasId) {
		try {
			clientInstallationService.removeInstallations(aliasId);
			return Response.ok(EmptyJSON.STRING).build();
		} catch (Exception e) {
			logger.error("Cannot remove device by alias {}", aliasId);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}

    }
}
