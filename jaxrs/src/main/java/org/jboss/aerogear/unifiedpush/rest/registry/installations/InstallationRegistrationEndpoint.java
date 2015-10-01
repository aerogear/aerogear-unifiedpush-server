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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.validation.DeviceTokenValidator;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.EmptyJSON;
import org.jboss.aerogear.unifiedpush.rest.util.ClientAuthHelper;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.metrics.PushMessageMetricsService;
import org.jboss.aerogear.unifiedpush.utils.AeroGearLogger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qmino.miredot.annotations.BodyType;
import com.qmino.miredot.annotations.ReturnType;

@Path("/registry/device")
public class InstallationRegistrationEndpoint extends AbstractBaseEndpoint {

    // at some point we should move the mapper to a util class.?
    public static final ObjectMapper mapper = new ObjectMapper();

    private final AeroGearLogger logger = AeroGearLogger.getInstance(InstallationRegistrationEndpoint.class);
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
     *****/
    @OPTIONS
    @Path("{token: .*}")
    @ReturnType("java.lang.Void")
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
    @ReturnType("java.lang.Void")
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
     * @HTTP 200 (OK) Successful storage of the device metadata.
     * @HTTP 400 (Bad Request) The format of the client request was incorrect (e.g. missing required values).
     * @HTTP 401 (Unauthorized) The request requires authentication.
     *
     * @param entity    {@link Installation} for Device registration
     * @return          registered {@link Installation}
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
    @ReturnType("org.jboss.aerogear.unifiedpush.api.Installation")
    public Response registerInstallation(
            Installation entity,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
        if (variant == null) {
            return create401Response(request);
        }

        // Poor up-front validation for required token
        final String deviceToken = entity.getDeviceToken();
        if (deviceToken == null || !DeviceTokenValidator.isValidDeviceTokenForVariant(deviceToken, variant.getType())) {
            logger.finest("Invalid device token was delivered: " + deviceToken);
            return appendAllowOriginHeader(Response.status(Status.BAD_REQUEST), request);
        }

        // The 'mobile application' on the device/client was launched.
        // If the installation is already in the DB, let's update the metadata,
        // otherwise we register a new installation:
        logger.finest("Mobile Application on device was launched");

        // async:
        clientInstallationService.addInstallation(variant, entity);

        return appendAllowOriginHeader(Response.ok(entity), request);
    }
    
    @POST
    @Path("/associate/{id: .*}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.Installation")
    public Response associate(Installation entity,
                           @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
        if (variant == null) {
            return create401Response(request);
        }
        
        Installation installation = clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), entity.getDeviceToken());
        if (installation == null) {
            return create401Response(request);
        }

        clientInstallationService.associateInstallation(installation);
 
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
     * @HTTP 200 (OK) Successful indicated that application was opened due to push.
     * @HTTP 401 (Unauthorized) The request requires authentication.
     *
     * @param pushMessageId push message identifier
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
    @ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
    public Response increasePushMessageReadCounter(@PathParam("id") String pushMessageId,
                           @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
        if (variant == null) {
            return create401Response(request);
        }

        //let's do update the analytics
        if (pushMessageId != null) {
            metricsService.updateAnalytics(pushMessageId, variant.getVariantID());
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
     * @HTTP 204 (OK) Successful unregistration.
     * @HTTP 401 (Unauthorized) The request requires authentication.
     * @HTTP 404 (Not Found) The requested device metadata does not exist.
     *
     * @param token device token
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
    @ReturnType("java.lang.Void")
    public Response unregisterInstallations(
            @PathParam("token") String token,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
        if (variant == null) {
            return create401Response(request);
        }

        // look up all installations (with same token) for the given variant:
        Installation installation =
                clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), token);

        if (installation == null) {
            return appendAllowOriginHeader(Response.status(Status.NOT_FOUND), request);
        } else {
            logger.info("Deleting metadata Installation");
            // remove
            clientInstallationService.removeInstallation(installation);
        }

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
     * <p/>
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
     * @HTTP 200 (OK) Successful submission of import job.
     * @HTTP 400 (Bad Request) The format of the client request was incorrect.
     * @HTTP 401 (Unauthorized) The request requires authentication.
     *
     * @param form  JSON file to import
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
    @BodyType("org.jboss.aerogear.unifiedpush.rest.registry.installations.ImporterForm")
    @ReturnType("org.jboss.aerogear.unifiedpush.rest.EmptyJSON")
    public Response importDevice(
            @MultipartForm
            ImporterForm form,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request);
        if (variant == null) {
            return create401Response(request);
        }

        List<Installation> devices;
        try {
            devices = mapper.readValue(form.getJsonFile(), new TypeReference<List<Installation>>() {});
        } catch (IOException e) {
            logger.severe("Error when parsing importer json file", e);

            return Response.status(Status.BAD_REQUEST).build();
        }

        logger.info("Devices to import: " + devices.size());

        clientInstallationService.addInstallations(variant, devices);

        // return directly, the above is async and may take a bit :-)
        return Response.ok(EmptyJSON.STRING).build();
    }

    private ResponseBuilder appendPreflightResponseHeaders(HttpHeaders headers, ResponseBuilder response) {
        // add response headers for the preflight request
        // required
        response.header("Access-Control-Allow-Origin", headers.getRequestHeader("Origin").get(0)) // return submitted origin
                .header("Access-Control-Allow-Methods", "POST, DELETE") // only POST/DELETE are allowed
                .header("Access-Control-Allow-Headers", "accept, origin, content-type, authorization") // explicit Headers!
                .header("Access-Control-Allow-Credentials", "true")
                // indicates how long the results of a preflight request can be cached (in seconds)
                .header("Access-Control-Max-Age", "604800"); // for now, we keep it for seven days

        return response;
    }

    private Response appendAllowOriginHeader(ResponseBuilder rb, HttpServletRequest request) {

        return rb.header("Access-Control-Allow-Origin", request.getHeader("Origin")) // return submitted origin
                .header("Access-Control-Allow-Credentials", "true")
                 .build();
    }

    private Response create401Response(final HttpServletRequest request) {
        return appendAllowOriginHeader(
                Response.status(Status.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                        .entity("Unauthorized Request"),
                request);
    }
}
