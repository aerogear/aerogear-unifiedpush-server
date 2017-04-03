package org.jboss.aerogear.unifiedpush.rest.util;

import javax.servlet.http.HttpServletRequest;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.RestWebApplication;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAuthHelper {
	public static final String DEVICE_TOKEN_HEADER = "device-token";

	private static final Logger logger = LoggerFactory.getLogger(ClientAuthHelper.class);


	public static Variant loadVariantWhenAuthorized(GenericVariantService genericVariantService,
			HttpServletRequest request) {
		return loadVariantWhenAuthorized(genericVariantService, request, true);
	}

	/**
	 * returns application if the masterSecret is valid for the request
	 * PushApplicationEntity
	 */
	public static Variant loadVariantWhenAuthorized(GenericVariantService genericVariantService,
			HttpServletRequest request, boolean logUnAuthorized) {
		// extract the pushApplicationID and its secret from the HTTP Basic
		// header:
		String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
		String variantID = credentials[0];
		String secret = credentials[1];

		final Variant variant = genericVariantService.findByVariantID(variantID);
		if (variant != null && variant.getSecret().equals(secret)) {
			return variant;
		}

		if (logUnAuthorized)
			logger.warn("UnAuthorized authentication using variantID: {}, Secret: {}", variantID, secret);
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

	// Barear authentication allowed only using /upsi context
	public static boolean isWebAppContext(HttpServletRequest request) {
		return request.getRequestURI().indexOf(RestWebApplication.UPSI_BASE_CONTEXT) > -1;
	}
}
