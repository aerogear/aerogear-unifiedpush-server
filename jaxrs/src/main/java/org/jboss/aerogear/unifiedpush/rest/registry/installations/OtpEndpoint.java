package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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

import org.jboss.aerogear.unifiedpush.api.InstallationVerificationAttempt;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullUUID;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.service.VerificationService;
import org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult;
import org.springframework.stereotype.Controller;

import com.qmino.miredot.annotations.ReturnType;

@Controller
@Path("/otp")
public class OtpEndpoint extends AbstractBaseEndpoint {
	@Inject
	private VerificationService verificationService;

	/**
	 * Cross Origin for OTP
	 *
	 * @param headers "Origin" header
	 * @return "Access-Control-Allow-Origin" header for your response
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin" header
	 * @responseheader Access-Control-Allow-Methods POST, DELETE, PUT, GET, OPTIONS
	 * @responseheader Access-Control-Allow-Headers accept, origin, content-type,
	 *                 authorization
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader Access-Control-Max-Age 604800
	 *
	 * @statuscode 200 Successful response for your request
	 */
	@OPTIONS
	@ReturnType("java.lang.Void")
	public Response crossOriginForInstallations(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	@GET
	@Path("/{alias}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resendVerificationCode(@PathParam("alias") String alias, @Context HttpServletRequest request) {

		String code = verificationService.initiateDeviceVerification(alias);
		if (code == null) {
			return appendAllowOriginHeader(
					Response.status(Status.BAD_REQUEST).entity(quote("Unable to send alias OTP code, missing alias")),
					request);
		}

		return appendAllowOriginHeader(Response.noContent(), request);
	}

	@POST
	@Path("/verify")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult")
	public Response enable(InstallationVerificationAttempt verificationAttempt,
			@DefaultValue("false") @QueryParam("reset") boolean reset, @Context HttpServletRequest request) {

		VerificationResult result = verificationService.verifyDevice(verificationAttempt.getDeviceToken(),
				NullUUID.NULL.getUuid(), verificationAttempt, reset);

		if (result == null) {
			return appendAllowOriginHeader(
					Response.status(Status.BAD_REQUEST).entity(quote("Unable to verify alias OTP code, missing alias")),
					request);
		}

		return appendAllowOriginHeader(Response.ok(result), request);
	}
}
