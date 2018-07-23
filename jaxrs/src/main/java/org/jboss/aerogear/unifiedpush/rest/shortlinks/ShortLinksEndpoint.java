package org.jboss.aerogear.unifiedpush.rest.shortlinks;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.rest.AbstractEndpoint;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.DocumentService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.keycloak.common.VerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.qmino.miredot.annotations.ReturnType;

@Controller
@Path("/shortlinks")
public class ShortLinksEndpoint extends AbstractEndpoint {
	private final Logger logger = LoggerFactory.getLogger(ShortLinksEndpoint.class);

	@Inject
	private DocumentService documentService;
	@Inject
	private IKeycloakService keycloakService;
	@Inject
	private AliasService aliasService;

	/**
	 * Cross Origin for shortlinks requests.
	 *
	 * @param headers
	 *            "Origin" header
	 * @return "Access-Control-Allow-Origin" header for your response
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin" header
	 * @responseheader Access-Control-Allow-Methods POST, PUT, DELETE, HEAD
	 * @responseheader Access-Control-Allow-Headers accept, origin, content-type,
	 *                 authorization
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader Access-Control-Max-Age 604800
	 *
	 * @statuscode 200 Successful response for your request
	 */
	@OPTIONS
	@ReturnType("java.lang.Void")
	public Response crossOriginForApplication(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	/**
	 * Cross Origin for shortlink new request.
	 *
	 * @param headers
	 *            "Origin" header
	 * @return "Access-Control-Allow-Origin" header for your response
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin" header
	 * @responseheader Access-Control-Allow-Methods POST, DELETE
	 * @responseheader Access-Control-Allow-Headers accept, origin, content-type,
	 *                 authorization
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader Access-Control-Max-Age 604800
	 *
	 * @statuscode 200 Successful response for your request
	 */
	@OPTIONS
	@Path("/{alias}")
	@ReturnType("java.lang.Void")
	public Response crossOriginForApplicationPut(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	@PUT	
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ReturnType("java.lang.Void")
	@Path("/{alias}")
	public Response saveForAlias(@PathParam("alias") String alias, //
			@FormParam("jwt") String token, //
			@Context HttpServletRequest request) { //

		// Get device-token authentication
		try {
			keycloakService.validateEmailActionToken(token);
		} catch (VerificationException e) {
			logger.warn("Unable to verify JWT token");
			logger.debug("Unable to verify JWT token", e);
		}
		
		// TODO - store json for future usage.
		// Extract jwt expiration and use it as document ttl
		
		try {
			return appendAllowOriginHeader(Response.noContent(), request);
		} catch (Exception ex) {
			return appendAllowOriginHeader(Response.status(Status.INTERNAL_SERVER_ERROR), request);
		}
	}
}
