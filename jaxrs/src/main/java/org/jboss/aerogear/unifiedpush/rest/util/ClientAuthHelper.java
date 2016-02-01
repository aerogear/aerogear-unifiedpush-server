package org.jboss.aerogear.unifiedpush.rest.util;

import javax.servlet.http.HttpServletRequest;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;

public class ClientAuthHelper {
	
	private static final String DEVICE_TOKEN_HEADER = "device-token";

	/**
	 * Returns the variant if the master secret is valid for the request and the device token
	 * in the request is installed for that variant
	 */
	public static Variant loadVariantWhenInstalled(GenericVariantService genericVariantService,
			ClientInstallationService clientInstallationService, HttpServletRequest request) {
		String deviceToken = request.getHeader(DEVICE_TOKEN_HEADER);
		if (deviceToken == null) {
			return null;
		}
		Variant variant = loadVariantWhenAuthorized(genericVariantService, request);
		if (variant == null) {
			return null;
		}
		Installation installation = clientInstallationService.findEnabledInstallationForVariantByDeviceToken(variant.getVariantID(), 
			HttpBasicHelper.decodeBase64(deviceToken));
		
		return installation != null ? variant : null;
	}
	
	/**
     * returns application if the masterSecret is valid for the request
     * PushApplicationEntity
     */
    public static Variant loadVariantWhenAuthorized(GenericVariantService genericVariantService, HttpServletRequest request) {
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
    
    public static String getDeviceToken(HttpServletRequest request) {
    	String deviceToken = request.getHeader(DEVICE_TOKEN_HEADER);
    	if (deviceToken == null) {
    		return null;
    	}
    	return HttpBasicHelper.decodeBase64(deviceToken);
    }
}
