package org.jboss.aerogear.unifiedpush.rest.util;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Invocation.Builder;

import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAuthHelper {
	private static final Logger logger = LoggerFactory.getLogger(ClientAuthHelper.class);

	private static final String DEVICE_TOKEN_HEADER = "device-token";

	/*
	 * Extract variantId / password from basic authentication and validate
	 * variant existence.
	 */
	public static Variant loadVariantWhenAuthorized(GenericVariantService genericVariantService,
			HttpServletRequest request) {
		return loadVariantWhenAuthorized(genericVariantService, request, true);
	}

	/*
	 * Extract variantId / password from basic authentication and validate
	 * variant existence.
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

	public static Builder setDeviceToken(Builder request, String deviceToken) {
		return request.header(DEVICE_TOKEN_HEADER, HttpBasicHelper.encodeBase64(deviceToken));
	}

}
