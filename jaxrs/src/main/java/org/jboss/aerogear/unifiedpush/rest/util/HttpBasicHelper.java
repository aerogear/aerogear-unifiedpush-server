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
package org.jboss.aerogear.unifiedpush.rest.util;

import net.iharder.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

import java.io.IOException;


public final class HttpBasicHelper {

    private HttpBasicHelper() {
    }

    private static boolean isBasic(String authorizationHeader) {
        return authorizationHeader.startsWith("Basic ");
    }

    private static String getAuthorizationHeader(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public static String[] extractUsernameAndPasswordFromBasicHeader(HttpServletRequest request) {
        String username = "";
        String password = "";
        String authorizationHeader = getAuthorizationHeader(request);

        if (authorizationHeader != null && isBasic(authorizationHeader)) {
            String base64Token = authorizationHeader.substring(6);
            String token = "";
            try {
                token = new String(Base64.decode(base64Token));
            } catch (IOException e) {
                //
            }

            int delimiter = token.indexOf(':');

            if (delimiter != -1) {
                username = token.substring(0, delimiter);
                password = token.substring(delimiter + 1);
            }
        }
        return new String[] { username, password };
    }
    
    /**
     * returns application if the masterSecret is valid for the request PushApplicationEntity
     */
    public static PushApplication loadPushApplicationWhenAuthorized(PushApplicationService pushApplicationService, HttpServletRequest request) {
        // extract the pushApplicationID and its secret from the HTTP Basic header:
        String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
        String pushApplicationID = credentials[0];
        String secret = credentials[1];

        final PushApplication pushApplication = pushApplicationService.findByPushApplicationID(pushApplicationID);
        if (pushApplication != null && pushApplication.getMasterSecret().equals(secret)) {
            return pushApplication;
        }

        // unauthorized...
        return null;
    }
    
    public static Response createRequestIsUnauthorizedResponse() {
    	return Response.status(Status.UNAUTHORIZED)
                .header("WWW-Authenticate", "Basic realm=\"AeroGear UnifiedPush Server\"")
                .entity("Unauthorized Request")
                .build();
    }
}
