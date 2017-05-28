package org.jboss.aerogear.unifiedpush.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.datastax.driver.core.utils.UUIDs;
import com.qmino.miredot.annotations.ReturnType;

@Path("/hartbeat")
public class HartbeatEndpoint extends AbstractBaseEndpoint {
	/**
	 * Hartbeat Endpoint
	 *
	 * @return Hartbeat in form of time-based UUID.
	 * @statuscode 200 Successful response for your request
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.String")
	public Response hartbeat(@Context HttpServletRequest request) {
		return appendAllowOriginHeader(Response.ok().entity(quote(UUIDs.timeBased().toString())), request);
	}
}
