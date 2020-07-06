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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.validation.DeviceTokenValidator;
import org.jboss.aerogear.unifiedpush.auth.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.util.error.ErrorBuilder;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.metrics.PrometheusExporter;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.List;

@Path("/registry/device")
public class InstallationRegistrationEndpoint extends AbstractBaseEndpoint {

    // at some point we should move the mapper to a util class.?
    public static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(InstallationRegistrationEndpoint.class);
    @Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private GenericVariantService genericVariantService;

    @Inject
    private PushMessageMetricsService metricsService;

    /**
     * Cross Origin for Installations
     *
     * @param headers   "Origin" header
     * @param token     token
     * @return          "Access-Control-Allow-Origin" header for your response
     *
     * @responseheader Access-Control-Allow-Origin      With host in your "Origin" header
     * @responseheader Access-Control-Allow-Methods     POST, DELETE
     * @responseheader Access-Control-Allow-Headers     accept, origin, content-type, authorization
     * @responseheader Access-Control-Allow-Credentials true
     * @responseheader Access-Control-Max-Age           604800
     *
     * @statuscode 200 Successful response for your request
     */
    @OPTIONS
    @Path("{token: .*}")
    public Response crossOriginForInstallations(
            @Context HttpHeaders headers,
            @PathParam("token") String token) {
        return appendPreflightResponseHeaders(headers, Response.ok()).build();
    }

    /**
     * Cross Origin for Installations
     *
     * @param headers   "Origin" header
     * @return          "Access-Control-Allow-Origin" header for your response
     *
     * @responseheader Access-Control-Allow-Origin      With host in your "Origin" header
     * @responseheader Access-Control-Allow-Methods     POST, DELETE
     * @responseheader Access-Control-Allow-Headers     accept, origin, content-type, authorization
     * @responseheader Access-Control-Allow-Credentials true
     * @responseheader Access-Control-Max-Age           604800
     *
     * @statuscode 200 Successful response for your request
     */
    @OPTIONS
    public Response crossOriginForInstallations(@Context HttpHeaders headers) {
        return appendPreflightResponseHeaders(headers, Response.ok()).build();
    }

    /**
     * RESTful API for Device registration.
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
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
     * @param oldToken  The previously registered deviceToken or an empty String.  Provided by the header x-ag-old-token.
     * @param entity    {@link Installation} for Device registration
     * @param request   the request object
     * @return          registered {@link Installation}
     *
     * @requestheader x-ag-old-token the old push service dependant token (ie InstanceID in FCM). If present these tokens will be forcefully unregistered before the new token is registered.
     *
     * @responseheader Access-Control-Allow-Origin      With host in your "Origin" header
     * @responseheader Access-Control-Allow-Credentials true
     * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server" (only for 401 response)
     *
     * @statuscode 200 Successful storage of the device metadata
     * @statuscode 400 The format of the client request was incorrect (e.g. missing required values)
     * @statuscode 401 The request requires authentication
     */

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerInstallation(
            @DefaultValue("") @HeaderParam("x-ag-old-token") final String oldToken,
            Installation entity,
            @Context HttpServletRequest request) {

        PrometheusExporter.instance().increaseTotalDeviceRegisterRequests();

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return create401Response(request);
        }

        // Poor up-front validation for required token
        final String deviceToken = entity.getDeviceToken();
        if (deviceToken == null || !DeviceTokenValidator.isValidDeviceTokenForVariant(deviceToken, variant.getType())) {
            logger.trace("Invalid device token was delivered: {} for variant type: {}", deviceToken, variant.getType());
            return appendAllowOriginHeader(Response.status(Status.BAD_REQUEST), request);
        }

        // The 'mobile application' on the device/client was launched.
        // If the installation is already in the DB, let's update the metadata,
        // otherwise we register a new installation:
        logger.trace("Mobile Application on device was launched");

        //The token has changed, remove the old one
        if (!oldToken.isEmpty() && !oldToken.equals(entity.getDeviceToken())) {
            logger.info("Deleting old device token {}", oldToken);
            clientInstallationService.removeInstallationForVariantByDeviceToken(variant.getVariantID(), oldToken);
        }

        logger.trace("Adding new device to {} variant", variant.getName());
        // async:
        clientInstallationService.addInstallation(variant, entity);

        return appendAllowOriginHeader(Response.ok(entity), request);
    }

    /**
     * RESTful API for Push Notification metrics registration.
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
     *
     * <pre>
     * curl -u "variantID:secret"
     *   -v -H "Accept: application/json" -H "Content-type: application/json" -H "aerogear-push-id: someid"
     *   -X PUT
     *   https://SERVER:PORT/context/rest/registry/device/pushMessage/{pushMessageId}
     * </pre>
     *
     * @param pushMessageId push message identifier
     * @param request the request
     * @return              empty JSON body
     *
     * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server" (only for 401 response)
     *
     * @statuscode 200 Successful storage of the device metadata
     * @statuscode 401 The request requires authentication
     */
    @PUT
    @Path("/pushMessage/{id: .*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response increasePushMessageReadCounter(@PathParam("id") String pushMessageId,
                           @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return create401Response(request);
        }

        //let's do update the analytics
        if (pushMessageId != null) {
            logger.trace("Push Notification '{}' was used to open the application on the device", pushMessageId);
            metricsService.updateAnalytics(pushMessageId);
        }

        return Response.ok(EmptyJSON.STRING).build();
    }

    /**
     * RESTful API for Device unregistration.
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
     *
     * <pre>
     * curl -u "variantID:secret"
     *   -v -H "Accept: application/json" -H "Content-type: application/json"
     *   -X DELETE
     *   https://SERVER:PORT/context/rest/registry/device/{token}
     * </pre>
     *
     * @param token device token
     * @param request the request
     * @return empty json
     *
     * @responseheader Access-Control-Allow-Origin      With host in your "Origin" header
     * @responseheader Access-Control-Allow-Credentials true
     * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server" (only for 401 response)
     *
     * @statuscode 204 Successful unregistration
     * @statuscode 401 The request requires authentication
     * @statuscode 404 The requested device metadata does not exist
     */
    @DELETE
    @Path("{token: .*}")
    public Response unregisterInstallations(
            @PathParam("token") String token,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return create401Response(request);
        }

        // look up all installations (with same token) for the given variant:
        Installation installation =
                clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), token);

        if (installation == null) {
            return appendAllowOriginHeader(Response.status(Status.NOT_FOUND), request);
        }

        logger.info("Deleting metadata Installation");
        // remove
        clientInstallationService.removeInstallation(installation);

        return appendAllowOriginHeader(Response.noContent(), request);
    }

    /**
     * API for uploading JSON file to allow massive device registration (aka import).
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
     *
     * <pre>
     * curl -u "variantID:secret"
     *   -v -H "Accept: application/json" -H "Content-type: multipart/form-data"
     *   -F "file=@/path/to/my-devices-for-import.json"
     *   -X POST
     *   https://SERVER:PORT/context/rest/registry/device/importer
     * </pre>
     *
     * The format of the JSON file is an array, containing several objects that follow the same syntax used on the
     * <code>/rest/registry/device</code> endpoint.
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
     * @param form  JSON file to import
     * @param request the request
     * @return      empty JSON body
     *
     * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server" (only for 401 response)
     *
     * @statuscode 200 Successful submission of import job
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 401 The request requires authentication
     */
    @POST
    @Path("/importer")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importDevice(
            @MultipartForm
            ImporterForm form,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return create401Response(request);
        }

        List<Installation> devices;
        try {
            devices = mapper.readValue(form.getJsonFile(), new TypeReference<List<Installation>>() {});
        } catch (IOException e) {
            logger.error("Error when parsing importer json file", e);

            return Response.status(Status.BAD_REQUEST).build();
        }

        logger.info("Devices to import: {}", devices.size());

        clientInstallationService.addInstallations(variant, devices);

        // return directly, the above is async and may take a bit :-)
        return Response.ok(EmptyJSON.STRING).build();
    }

    private static ResponseBuilder appendPreflightResponseHeaders(final HttpHeaders headers, final ResponseBuilder response) {
        // add response headers for the preflight request
        // required
        response.header("Access-Control-Allow-Methods", "POST, DELETE") // only POST/DELETE are allowed
                .header("Access-Control-Allow-Headers", "accept, origin, content-type, authorization") // explicit Headers!
                .header("Access-Control-Allow-Credentials", "true")
                // indicates how long the results of a preflight request can be cached (in seconds)
                .header("Access-Control-Max-Age", "604800"); // for now, we keep it for seven days

        return response;
    }

    private static Response appendAllowOriginHeader(final ResponseBuilder rb, final HttpServletRequest request) {

        return rb.header("Access-Control-Allow-Credentials", "true")
                 .build();
    }

    private static Response create401Response(final HttpServletRequest request) {
        return appendAllowOriginHeader(
                Response.status(Status.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                        .entity(ErrorBuilder.forAuth().unauthorized().build()),
                request);
    }

    /**
     * returns application if the masterSecret is valid for the request
     * PushApplicationEntity
     */
    private Variant loadVariantWhenAuthorized(final HttpServletRequest request) {
        // extract the pushApplicationID and its secret from the HTTP Basic
        // header:
        final String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
        final String variantID = credentials[0];
        final String secret = credentials[1];

        final Variant variant = genericVariantService.findByVariantID(variantID);
        if (variant != null && variant.getSecret().equals(secret)) {
            return variant;
        }

        // unauthorized...
        return null;
    }
}
