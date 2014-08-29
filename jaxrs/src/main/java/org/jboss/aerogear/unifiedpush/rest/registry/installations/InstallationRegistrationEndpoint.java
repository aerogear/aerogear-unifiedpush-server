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
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Path("/registry/device")
@TransactionAttribute
public class InstallationRegistrationEndpoint {

    // at some point we should move the mapper to a util class.?
    public static final ObjectMapper mapper = new ObjectMapper();

    private final Logger logger = Logger.getLogger(InstallationRegistrationEndpoint.class.getName());
    @Inject
    private ClientInstallationService clientInstallationService;
    @Inject
    private GenericVariantService genericVariantService;

    @OPTIONS
    @Path("{token: .*}")
    public Response crossOriginForInstallations(
            @Context HttpHeaders headers,
            @PathParam("token") String token) {

        return appendPreflightResponseHeaders(headers, Response.ok()).build();
    }

    @OPTIONS
    public Response crossOriginForInstallations(@Context HttpHeaders headers) {

        return appendPreflightResponseHeaders(headers, Response.ok()).build();
    }

    /**
     * RESTful API for Device registration.
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
     *
     * <pre>
     * curl -3 -u "variantID:secret"
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
     *   https://SERVER:PORT/context/rest/registry/device
     * </pre>
     *
     * Details about JSON format can be found HERE!
     *
     * @HTTP 200 (OK) Successful storage of the device metadata.
     * @HTTP 400 (Bad Request) The format of the client request was incorrect (e.g. missing required values).
     * @HTTP 401 (Unauthorized) The request requires authentication.
     * @HTTP 404 (Not Found) The requested Variant resource does not exist.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerInstallation(
            Installation entity,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return appendAllowOriginHeader(
                    Response.status(Status.UNAUTHORIZED)
                            .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                            .entity("Unauthorized Request"),
                    request);
        }

        // Poor validation: We require the Token
        if (entity.getDeviceToken() == null || entity.getDeviceToken().isEmpty()) {
            return appendAllowOriginHeader(Response.status(Status.BAD_REQUEST), request);
        }

        // The 'mobile application' on the device/client was launched.
        // If the installation is already in the DB, let's update the metadata,
        // otherwise we register a new installation:
        logger.log(Level.FINEST, "Mobile Application on device was launched");

        // async:
        clientInstallationService.addInstallation(variant, entity);

        return appendAllowOriginHeader(Response.ok(entity), request);
    }

    /**
     * RESTful API for Device unregistration.
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>VariantID:secret</code>).
     *
     * <pre>
     * curl -3 -u "variantID:secret"
     *   -v -H "Accept: application/json" -H "Content-type: application/json"
     *   -X DELETE
     *   https://SERVER:PORT/context/rest/registry/device/{token}
     * </pre>
     *
     * @HTTP 204 (OK) Successful unregistration.
     * @HTTP 401 (Unauthorized) The request requires authentication.
     * @HTTP 404 (Not Found) The requested device metadata does not exist.
     */
    @DELETE
    @Path("{token: .*}")
    public Response unregisterInstallations(
            @PathParam("token") String token,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return appendAllowOriginHeader(
                    Response.status(Status.UNAUTHORIZED)
                            .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                            .entity("Unauthorized Request"),
                    request);
        }

        // look up all installations (with same token) for the given variant:
        Installation installation =
                clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), token);

        if (installation == null) {
            return appendAllowOriginHeader(Response.status(Status.NOT_FOUND), request);
        } else {
            logger.log(Level.INFO, "Deleting metadata Installation");
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
     * curl -3 -u "variantID:secret"
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
     * @HTTP 404 (Not Found) The requested Variant resource does not exist.
     */
    @POST
    @Path("/importer")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importDevice(
            @MultipartForm
            ImporterForm form,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return appendAllowOriginHeader(
                    Response.status(Status.UNAUTHORIZED)
                            .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                            .entity("Unauthorized Request"),
                    request);
        }

        List<Installation> devices;
        try {
            devices = mapper.readValue(form.getJsonFile(), new TypeReference<List<Installation>>() {});
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error when parsing importer json file", e);

            return Response.status(Status.BAD_REQUEST).build();
        }

        logger.log(Level.INFO, "Devices to import: " + devices.size());

        clientInstallationService.addInstallations(variant, devices);

        // return directly, the above is async and may take a bit :-)
        return Response.status(Status.OK)
                .entity("Job submitted for processing").build();
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

    /**
     * returns application if the masterSecret is valid for the request
     * PushApplicationEntity
     */
    private Variant loadVariantWhenAuthorized(
            HttpServletRequest request) {
        // extract the pushApplicationID and its secret from the HTTP Basic
        // header:
        String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
        String variantID = credentials[0];
        String secret = credentials[1];

        final Variant variant = genericVariantService.findByVariantID(variantID);
        if (variant != null && variant.getSecret().equals(secret)) {
            return variant;
        }

        // unauthorized...
        return null;
    }
}