package org.aergear.ups.rest.device;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.prometheus.client.Counter;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.validation.DeviceTokenValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;

import static org.aergear.ups.utils.HttpServletRequestUtils.*;

@Path("/registry/device")
public class DeviceRegistrationEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(DeviceRegistrationEndpoint.class);
    private static final Counter promDeviceRegisterRequestsTotal = Counter.build()
            .name("device_register_requests_total")
            .help("Total number of Device register requests.")
            .register();

    public static final String KAFKA_INSTALLATION_TOPIC = "agpush_installationMetrics";
    public static final ObjectMapper MAPPER = new ObjectMapper();


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

        promDeviceRegisterRequestsTotal.inc();

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return create401Response(request);
        }

        // Poor up-front validation for required token
        final String deviceToken = entity.getDeviceToken();
        if (deviceToken == null || !DeviceTokenValidator.isValidDeviceTokenForVariant(deviceToken, variant.getType())) {
            logger.trace(String.format("Invalid device token was delivered: %s for variant type: %s", deviceToken, variant.getType()));
            return appendAllowOriginHeader(Response.status(Response.Status.BAD_REQUEST), request);
        }

        // The 'mobile application' on the device/client was launched.
        // If the installation is already in the DB, let's update the metadata,
        // otherwise we register a new installation:
        logger.trace("Mobile Application on device was launched");

        //The token has changed, remove the old one
        if (!oldToken.isEmpty() && !oldToken.equals(entity.getDeviceToken())) {
            logger.info(String.format("Deleting old device token %s", oldToken));
            //clientInstallationService.removeInstallationForVariantByDeviceToken(variant.getVariantID(), oldToken);
        }

        // async:
        //clientInstallationService.addInstallation(variant, entity);

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
                                                   @Context HttpServletRequest request) throws IOException {

        // find the matching variation:
        final Variant variant = loadVariantWhenAuthorized(request);
        if (variant == null) {
            return create401Response(request);
        }

        if (pushMessageId != null) {

            // start the producer and push a message to installation metrics
            // topic
            //installationMetricsProducer.send(KAFKA_INSTALLATION_TOPIC, pushMessageId);

            return Response.ok(EMPTY_JSON).build();

        } else {
            logger.warn("A request with empty push message id was done. Bad Request response is returned.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
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

//        // look up all installations (with same token) for the given variant:
//        final Installation installation = clientInstallationService.findInstallationForVariantByDeviceToken(variant.getVariantID(), token);
//
//        if (installation == null) {
//            return appendAllowOriginHeader(Response.status(Response.Status.NOT_FOUND), request);
//        }
//
//        logger.info("Deleting metadata Installation");
//        // remove
//        clientInstallationService.removeInstallation(installation);

        return appendAllowOriginHeader(Response.noContent(), request);
    }

    /**
     * returns application if the masterSecret is valid for the request
     * PushApplicationEntity
     */
    private Variant loadVariantWhenAuthorized(final HttpServletRequest request) {
        return new AndroidVariant();
//        // extract the pushApplicationID and its secret from the HTTP Basic
//        // header:
//        final String[] credentials = extractUsernameAndPasswordFromBasicHeader(request);
//        final String variantID = credentials[0];
//        final String secret = credentials[1];
//
//        final Variant variant = genericVariantService.findByVariantID(variantID);
//        if (variant != null && variant.getSecret().equals(secret)) {
//            return variant;
//        }
//
//        // unauthorized...
//        return null;
    }

}
