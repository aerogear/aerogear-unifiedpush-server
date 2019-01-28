/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
import org.jboss.aerogear.unifiedpush.api.InstallationVerificationAttempt;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.util.ClientAuthHelper;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.VerificationService;
import org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult;
import org.jboss.aerogear.unifiedpush.service.metrics.IPushMessageMetricsService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qmino.miredot.annotations.BodyType;
import com.qmino.miredot.annotations.ReturnType;

@Controller
@Path("/registry/device")
public class InstallationRegistrationEndpoint extends AbstractBaseRegistrationEndpoint {

	// at some point we should move the mapper to a util class.?
	public static final ObjectMapper mapper = new ObjectMapper();

	private final Logger logger = LoggerFactory.getLogger(InstallationRegistrationEndpoint.class);

	@Inject
	private GenericVariantService genericVariantService;
	@Inject
	private IPushMessageMetricsService metricsService;
	@Inject
	private VerificationService verificationService;

	/**
	 * Cross Origin for Installations
	 *
	 * @param headers "Origin" header
	 * @param token   Will match any pattern not matched by a more specific path.
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
	 *****/
	@OPTIONS
	@Path("{token: .*}")
	@ReturnType("java.lang.Void")
	public Response crossOriginForInstallations(@Context HttpHeaders headers, @PathParam("token") String token) {

		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	/**
	 * Cross Origin for Installations
	 *
	 * @param headers "Origin" header
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
	@ReturnType("java.lang.Void")
	public Response crossOriginForInstallations(@Context HttpHeaders headers) {
		return appendPreflightResponseHeaders(headers, Response.ok()).build();
	}

	/**
	 * RESTful API for Device registration. The Endpoint is protected using
	 * <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json" -H "aerogear-push-id: someid"
	 *   -X POST
	 *   -d '{
	 *     "deviceToken" : "someTokenString",
	 *     "deviceType" : "iPad",
	 *     "operatingSystem" : "iOS",
	 *     "osVersion" : "6.1.2",
	 *     "alias" : "someUsername or email adress...",
	 *     "categories" : ["football", "sport"]
	 *   }'
	 *   https://SERVER:PORT/context/rest/registry/device
	 * </pre>
	 *
	 * Details about JSON format can be found HERE!
	 *
	 * @param oldToken      The previously registered deviceToken or an empty
	 *                      String. Provided by the header x-ag-old-token.
	 * @param entity        {@link Installation} for Device registration
	 * @param synchronously force synchronous registration
	 * @param request       The request object
	 * @return Registered {@link Installation}
	 *
	 * @requestheader x-ag-old-token the old push service dependant token (ie
	 *                InstanceID in FCM). If present these tokens will be forcefully
	 *                unregistered before the new token is registered.
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin" header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroBase UnifiedPush Server"
	 *                 (only for 401 response)
	 *
	 * @statuscode 200 Successful storage of the device metadata
	 * @statuscode 400 The format of the client request was incorrect (e.g. missing
	 *             required values)
	 * @statuscode 401 The request requires authentication
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.api.Installation")
	public Response registerInstallation(@DefaultValue("") @HeaderParam("x-ag-old-token") final String oldToken,
			Installation entity, @DefaultValue("false") @QueryParam("synchronously") boolean synchronously,
			@Context HttpServletRequest request) {

		// find the matching variation:
		final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
		if (variant == null) {
			return create401Response(request);
		}

		return register(variant, oldToken, entity, synchronously, true, request);
	}

	/**
	 * RESTful API for updating device token. The Endpoint is protected using
	 * <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json" -H "device-token: some device token"
	 *   -X PUT
	 *   https://SERVER:PORT/context/rest/registry/device/token/{token: .*}
	 * </pre>
	 *
	 * @param token token to update
	 * @param request       the request
	 * @return empty JSON body
	 *
	 * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server"
	 *                 (only for 401 response)
	 *
	 * @statuscode 200 Successful storage of the device metadata
	 * @statuscode 401 The request requires authentication
	 */
	@PUT
	@Path("/token/{token: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	public Response updateDeviceToken(@PathParam("token") String token, @Context HttpServletRequest request) {

		// find the matching device by variation and token id.
		Optional<Installation> device = authenticationHelper.loadInstallationWhenAuthorized(request);
		if (!device.isPresent()) {
			return create401Response(request);
		}

		// Update device token
		Installation inst = device.get();
		inst.setDeviceToken(HttpBasicHelper.decodeBase64(token));

		try {
			clientInstallationService.updateInstallation(inst);
			logger.info("Device token was updated from {} to {}", ClientAuthHelper.getDeviceToken(request),
					inst.getDeviceToken());
		} catch (Exception e) {
			logger.error("Error when update device token", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return appendAllowOriginHeader(Response.noContent(), request);
	}

	/**
	 * RESTful API for Push Notification metrics registration. The Endpoint is
	 * protected using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json" -H "aerogear-push-id: someid"
	 *   -X PUT
	 *   https://SERVER:PORT/context/rest/registry/device/pushMessage/{pushMessageId}
	 * </pre>
	 *
	 * @param pushMessageId push message identifier
	 * @param request       the request
	 * @return empty JSON body
	 *
	 * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server"
	 *                 (only for 401 response)
	 *
	 * @statuscode 200 Successful storage of the device metadata
	 * @statuscode 401 The request requires authentication
	 */
	@PUT
	@Path("/pushMessage/{id: .*}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response increasePushMessageReadCounter(@PathParam("id") String pushMessageId,
			@Context HttpServletRequest request) {

		// find the matching variation:
		final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
		if (variant == null) {
			return create401Response(request);
		}

		// let's do update the analytics
		if (pushMessageId != null) {
			metricsService.updateAnalytics(pushMessageId);
		}

		return Response.ok(EmptyJSON.STRING).build();
	}

	/**
	 * RESTful API for Device unregistration. The Endpoint is protected using
	 * <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X DELETE
	 *   https://SERVER:PORT/context/rest/registry/device/{token}
	 * </pre>
	 *
	 * @param token   device token
	 * @param request the request
	 * @return empty json
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin" header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server"
	 *                 (only for 401 response)
	 *
	 * @statuscode 204 Successful unregistration
	 * @statuscode 401 The request requires authentication
	 * @statuscode 404 The requested device metadata does not exist
	 */
	@DELETE
	@Path("{token: .*}")
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Void")
	public Response unregisterInstallations(@PathParam("token") String token, @Context HttpServletRequest request) {

		// find the matching variation:
		final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
		if (variant == null) {
			return create401Response(request);
		}

		// look up all installations (with same token) for the given variant:
		Installation installation = clientInstallationService
				.findInstallationForVariantByDeviceToken(variant.getVariantID(), token);

		if (installation == null) {
			return appendAllowOriginHeader(Response.status(Status.NOT_FOUND), request);
		}

		logger.info("Deleting metadata Installation");
		// remove
		clientInstallationService.removeInstallation(installation);

		return appendAllowOriginHeader(Response.noContent(), request);
	}

	/**
	 * API for uploading JSON file to allow massive device registration (aka
	 * import). The Endpoint is protected using <code>HTTP Basic</code> (credentials
	 * <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: multipart/form-data"
	 *   -F "file=@/path/to/my-devices-for-import.json"
	 *   -X POST
	 *   https://SERVER:PORT/context/rest/registry/device/importer
	 * </pre>
	 *
	 * The format of the JSON file is an array, containing several objects that
	 * follow the same syntax used on the <code>/rest/registry/device</code>
	 * endpoint.
	 * <p>
	 * Here is an example:
	 *
	 * <pre>
	 * [
	 *   {
	 *     "deviceToken" : "someTokenString",
	 *     "deviceType" : "iPad",
	 *     "operatingSystem" : "iOS",
	 *     "osVersion" : "6.1.2",
	 *     "alias" : "someUsername or email adress...",
	 *     "categories" : ["football", "sport"]
	 *   },
	 *   {
	 *     "deviceToken" : "someOtherTokenString",
	 *     ...
	 *   },
	 *   ...
	 * ]
	 * </pre>
	 *
	 * @param form    JSON file to import
	 * @param request the request
	 * @return empty JSON body
	 *
	 * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server"
	 *                 (only for 401 response)
	 *
	 * @statuscode 200 Successful submission of import job
	 * @statuscode 400 The format of the client request was incorrect
	 * @statuscode 401 The request requires authentication
	 */
	@POST
	@Path("/importer")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@BodyType("org.jboss.aerogear.unifiedpush.rest.registry.installations.ImporterForm")
	@ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
	public Response importDevice(@MultipartForm ImporterForm form, @Context HttpServletRequest request) {

		// find the matching variation:
		final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
		if (variant == null) {
			return create401Response(request);
		}

		List<Installation> devices;
		try {
			devices = mapper.readValue(form.getJsonFile(), new TypeReference<List<Installation>>() {
			});
		} catch (IOException e) {
			logger.error("Error when parsing importer json file", e);

			return Response.status(Status.BAD_REQUEST).build();
		}

		logger.info("Devices to import: {}", devices.size());

		clientInstallationAsyncService.addInstallations(variant, devices);

		// return directly, the above is async and may take a bit :-)
		return Response.ok(EmptyJSON.STRING).build();
	}

	/**
	 * RESTful API for enabling a device (verifying it). The Endpoint is protected
	 * using <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json"
	 *   -X POST
	 *   -d '{
	 *     "deviceToken" : "someTokenString",
	 *     "deviceType" : "iPad",
	 *     "operatingSystem" : "iOS",
	 *     "osVersion" : "6.1.2",
	 *     "alias" : "someUsername or email adress...",
	 *     "categories" : ["football", "sport"]
	 *   }'
	 *   https://SERVER:PORT/context/rest/registry/enable
	 * </pre>
	 *
	 *
	 * @HTTP 200 (OK) for any verification result
	 * @HTTP 401 (Unauthorized) The request requires authentication.
	 *
	 * @param entity           {@link Installation} the device verifying
	 * @param verificationCode the verification code
	 * @return verification outcome {@link VerificationResult}
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin" header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server"
	 *                 (only for 401 response)
	 *
	 * @statuscode 200 Successful storage of the device metadata
	 * @statuscode 400 The format of the client request was incorrect (e.g. missing
	 *             required values)
	 * @statuscode 401 The request requires authentication
	 */
	@POST
	@Path("/enable")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult")
	public Response enable(InstallationVerificationAttempt verificationAttempt, @Context HttpServletRequest request) {

		// find the matching variation:
		final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
		if (variant == null) {
			return create401Response(request);
		}

		Installation installation = clientInstallationService
				.findInstallationForVariantByDeviceToken(variant.getVariantID(), verificationAttempt.getDeviceToken());

		if (installation == null) {
			return appendAllowOriginHeader(Response.status(Status.BAD_REQUEST)
					.entity(quote("installation not found for: " + verificationAttempt.getDeviceToken())), request);
		}

		VerificationResult result = verificationService.verifyDevice(installation, variant, verificationAttempt);

		return appendAllowOriginHeader(Response.ok(result), request);
	}

	/**
	 * RESTful API for resending a verification code. The Endpoint is protected
	 * using <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
	 *
	 * <pre>
	 * curl -u "variantID:secret" -H "deviceToken:<client device token>"
	 *   -v -H "Accept: application/json" -H "Content-type: application/json" -H
	 *   -X GET
	 *   https://SERVER:PORT/context/rest/registry/resendVerificationCode
	 * </pre>
	 *
	 *
	 * @HTTP 200 (OK) if resend went through.
	 * @HTTP 400 (Bad Request) deviceToken header not sent.
	 * @HTTP 401 (Unauthorized) The request requires authentication.
	 *
	 * @responseheader Access-Control-Allow-Origin With host in your "Origin" header
	 * @responseheader Access-Control-Allow-Credentials true
	 * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server"
	 *                 (only for 401 response)
	 *
	 * @statuscode 200 resend went through
	 * @statuscode 400 deviceToken header required.
	 * @statuscode 401 The request requires authentication
	 */
	@GET
	@Path("/resendVerificationCode")
	@Produces(MediaType.APPLICATION_JSON)
	public Response resendVerificationCode(@Context HttpServletRequest request) {

		final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
		if (variant == null) {
			return create401Response(request);
		}

		String deviceToken = ClientAuthHelper.getDeviceToken(request);
		if (deviceToken == null) {
			return appendAllowOriginHeader(
					Response.status(Status.BAD_REQUEST).entity(quote("deviceToken header required")), request);
		}

		// TODO - Support optional application id as query parameter.
		String code = verificationService.retryDeviceVerification(deviceToken, variant);
		if (code == null) {
			return appendAllowOriginHeader(
					Response.status(Status.BAD_REQUEST).entity(quote("Unable to find installation for device-token")),
					request);
		}

		return appendAllowOriginHeader(Response.ok(EmptyJSON.STRING), request);
	}

	@GET
	@Path("/associate")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("org.jboss.aerogear.unifiedpush.api.Variant")
	public Response associate(@Context HttpServletRequest request) {

		// find the matching variation:
		final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
		if (variant == null) {
			return create401Response(request);
		}

		String deviceToken = ClientAuthHelper.getDeviceToken(request);
		if (deviceToken == null) {
			return appendAllowOriginHeader(
					Response.status(Status.BAD_REQUEST).entity(quote("deviceToken header required")), request);
		}

		Installation installation = clientInstallationService
				.findInstallationForVariantByDeviceToken(variant.getVariantID(), deviceToken);

		if (installation == null) {
			return appendAllowOriginHeader(
					Response.status(Status.BAD_REQUEST).entity(quote("installation not found for: " + deviceToken)),
					request);
		}

		if (installation.isEnabled() == false) {
			return appendAllowOriginHeader(Response.status(Status.BAD_REQUEST)
					.entity(quote("unable to assosiate, device is disabled: " + deviceToken)), request);
		}

		// Associate the device - find the matching application and update the
		// device to the right application.
		Variant newVariant = clientInstallationService.associateInstallation(installation, variant);

		// Associate did not match to any alias
		if (newVariant == null) {
			return appendAllowOriginHeader(
					Response.status(Status.BAD_REQUEST).entity(
							quote("unable to assosiate, either alias is missing or can't find equivalent variant!")),
					request);
		}

		return appendAllowOriginHeader(Response.ok(newVariant), request);
	}

	@GET
	@Path("/exists")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@ReturnType("java.lang.Boolean")
	public Response exists(@Context HttpServletRequest request) {
		// Query installation if exists, enabled and authorized
		if (authenticationHelper.loadInstallationWhenAuthorized(request).isPresent()) {
			return appendAllowOriginHeader(Response.ok(Boolean.TRUE), request);
		}

		return appendAllowOriginHeader(Response.ok(Boolean.FALSE), request);
	}
}
