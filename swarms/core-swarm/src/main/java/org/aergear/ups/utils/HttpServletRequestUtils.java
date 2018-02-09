package org.aergear.ups.utils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Helper for various tasks for working with {@link javax.servlet.http.HttpServletRequest} objects.
 */
public final class HttpServletRequestUtils {

    private HttpServletRequestUtils() {
        // no-op
    }

    // from JSR 303
    private static final Pattern IP_ADDR_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");

    private static final String HTTP_BASIC_SCHEME = "Basic ";
    private static final String AUTH_HEADER = "Authorization";

    public static final String EMPTY_JSON = "{}";


    /**
     * Extracts the IP address from the given {@link javax.servlet.http.HttpServletRequest}.
     *
     * @param request to inspect
     *
     * @return the IP address from the given request
     */
    public static String extractIPAddress(final HttpServletRequest request) {

        String ip = request.getHeader("x-forwarded-for");
        if (isIPAdressValid(ip)) {
            return ip;
        }
        ip = request.getHeader("Proxy-Client-IP");
        if (isIPAdressValid(ip)) {
            return ip;
        }
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isIPAdressValid(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }

    /**
     * Reads the "aerogear-sender" header to check if an AeroGear Sender client was used. If the header value is NULL
     * the value of the standard "user-agent" header is returned
     *
     * @param request to inspect
     *
     * @return value of header
     */
    public static String extractAeroGearSenderInformation(final HttpServletRequest request) {
        final String client = request.getHeader("aerogear-sender");
        if (hasValue(client)) {
            return client;
        }
        // if there was no usage of our custom header, we simply return the user-agent value
        return request.getHeader("user-agent");
    }

    /**
     * Extraxts the Base64 decoded user/password pair from the Auth header.
     *
     * @param request to extract the values from
     * @return array container username/password pair
     */
    public static String[] extractUsernameAndPasswordFromBasicHeader(final HttpServletRequest request) {
        final String authorizationHeader = getAuthorizationHeader(request);
        String username = "";
        String password = "";

        if (authorizationHeader != null && isBasic(authorizationHeader)) {
            final String base64Token = authorizationHeader.substring(HTTP_BASIC_SCHEME.length());
            final String token = new String(Base64.getDecoder().decode(base64Token), StandardCharsets.UTF_8);

            int delimiter = token.indexOf(':');

            if (delimiter != -1) {
                username = token.substring(0, delimiter);
                password = token.substring(delimiter + 1);
            }
        }
        return new String[] { username, password };
    }

    public static Response.ResponseBuilder appendPreflightResponseHeaders(HttpHeaders headers, Response.ResponseBuilder response) {
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

    public static Response appendAllowOriginHeader(Response.ResponseBuilder rb, HttpServletRequest request) {

        return rb.header("Access-Control-Allow-Origin", request.getHeader("Origin")) // return submitted origin
                .header("Access-Control-Allow-Credentials", "true")
                .build();
    }

    public static Response create401Response(final HttpServletRequest request) {
        return appendAllowOriginHeader(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                        .entity("Unauthorized Request"),
                request);
    }

    private static boolean isIPAdressValid(final String ip){

        // InetAddress.getByName() validates 'null' as a valid IP (localhost).
        // we do not want that
        if (hasValue(ip)) {
            return IP_ADDR_PATTERN.matcher(ip).matches();
        }
        return false;
    }

    private static boolean hasValue(final String value) {
        return value != null && !value.isEmpty();
    }

    private static boolean isBasic(final String authorizationHeader) {
        return authorizationHeader.startsWith(HTTP_BASIC_SCHEME);
    }

    private static String getAuthorizationHeader(final HttpServletRequest request) {
        return request.getHeader(AUTH_HEADER);
    }

}
