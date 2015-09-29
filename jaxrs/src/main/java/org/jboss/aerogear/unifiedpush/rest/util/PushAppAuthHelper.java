package org.jboss.aerogear.unifiedpush.rest.util;

import javax.servlet.http.HttpServletRequest;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;

public class PushAppAuthHelper {
	
	/**
     * returns application if the masterSecret is valid for the request PushApplicationEntity
     */
	public static PushApplication loadPushApplicationWhenAuthorized(HttpServletRequest request, PushApplicationService pushApplicationService) {
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
}
