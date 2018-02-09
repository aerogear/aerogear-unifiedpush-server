package org.aergear.ups.rest.sender;

import io.prometheus.client.Counter;
import org.aergear.ups.internal.InternalUnifiedPushMessage;
import org.aergear.ups.utils.HttpServletRequestUtils;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.aergear.ups.utils.HttpServletRequestUtils.EMPTY_JSON;

@Path("/sender")
public class PushNotificationSenderEndpoint {

    public static final String KAFKA_PUSH_MESSAGE_PROCESSING_TOPIC = "agpush_pushMessageProcessing";

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationSenderEndpoint.class);
    private static final Counter promPrushRequestsTotal = Counter.build()
            .name("aerogear_ups_push_requests_total")
            .help("Total number of push requests.")
            .register();

//    @Producer
//    private SimpleKafkaProducer<PushApplication, InternalUnifiedPushMessage> pushMessageProcessingProducer;

    /**
     * RESTful API for sending Push Notifications.
     * The Endpoint is protected using <code>HTTP Basic</code> (credentials <code>PushApplicationID:masterSecret</code>).
     * <p>
     *
     * Messages are submitted as flexible JSON maps. Below is a simple example:
     * <pre>
     * curl -u "PushApplicationID:MasterSecret"
     *   -v -H "Accept: application/json" -H "Content-type: application/json"
     *   -X POST
     *   -d '{
     *     "message": {
     *      "alert": "HELLO!",
     *      "sound": "default",
     *      "user-data": {
     *          "key": "value",
     *      }
     *   }'
     *   https://SERVER:PORT/CONTEXT/rest/sender
     * </pre>
     *
     * Details about the Message Format can be found HERE!
     * <p>
     *
     * <b>Request Header</b> {@code aerogear-sender} uses to identify the used client. If the header is not present, the standard "user-agent" header is used.
     *
     * @param message   message to send
     * @param request the request
     * @return          empty JSON body
     *
     * @responseheader WWW-Authenticate Basic realm="AeroGear UnifiedPush Server" (only for 401 response)
     *
     * @statuscode 202 Indicates the Job has been accepted and is being process by the AeroGear UnifiedPush Server
     * @statuscode 401 The request requires authentication
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response send(final InternalUnifiedPushMessage message, @Context final HttpServletRequest request) {

        promPrushRequestsTotal.inc();

        final PushApplication pushApplication = loadPushApplicationWhenAuthorized(request);
//        if (pushApplication == null) {
//            return Response.status(Response.Status.UNAUTHORIZED)
//                    .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
//                    .entity("Unauthorized Request")
//                    .build();
//        }

        // submit http request metadata:
        message.setIpAddress(HttpServletRequestUtils.extractIPAddress(request));

        // add the client identifier
        message.setClientIdentifier(HttpServletRequestUtils.extractAeroGearSenderInformation(request));

        // start the producer and push a message to "agpush_pushMessageProcessing" topic
        //pushMessageProcessingProducer.send(KAFKA_PUSH_MESSAGE_PROCESSING_TOPIC, pushApplication, message);


        logger.debug(String.format("Push Message Request from [%s] API was internally submitted for further processing", message.getClientIdentifier()));

        return Response.status(Response.Status.ACCEPTED).entity(EMPTY_JSON).build();
    }

    /**
     * returns application if the masterSecret is valid for the request PushApplicationEntity
     */
    private PushApplication loadPushApplicationWhenAuthorized(final HttpServletRequest request) {
        // extract the pushApplicationID and its secret from the HTTP Basic header:
        final String[] credentials = HttpServletRequestUtils.extractUsernameAndPasswordFromBasicHeader(request);
        final String pushApplicationID = credentials[0];
        final String secret = credentials[1];

        final PushApplication pushApplication = null;//pushApplicationService.findByPushApplicationID(pushApplicationID);
        if (pushApplication != null && pushApplication.getMasterSecret().equals(secret)) {
            return pushApplication;
        }

        // unauthorized...
        return null;
    }

}
