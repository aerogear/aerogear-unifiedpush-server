package org.jboss.aerogear.unifiedpush.rest.util;


import javax.servlet.http.HttpServletRequest;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.RestWebApplication;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientAuthHelper {
	public static final String DEVICE_TOKEN_HEADER = "device-token";

	private static final Logger logger = LoggerFactory.getLogger(ClientAuthHelper.class);

	/**
	 * Returns the variant if the master secret is valid for the request and the
	 * device token in the request is installed for that variant
	 */
	public static Variant loadVariantWhenInstalled(GenericVariantService genericVariantService,
			ClientInstallationService clientInstallationService, HttpServletRequest request) {
		String deviceToken = request.getHeader(DEVICE_TOKEN_HEADER);
		if (deviceToken == null) {
			logger.info("API request missing " + DEVICE_TOKEN_HEADER + " header! URI - > " + request.getRequestURI());
			return null;
		}

		Variant variant = loadVariantWhenAuthorized(genericVariantService, request);

		if (variant == null && request.getRequestURI().indexOf(RestWebApplication.UPSI_BASE_CONTEXT) > -1) {
			variant = loadVariantFromBearerWhenAuthorized(genericVariantService, request);
			if (variant == null) {
				logger.info("API request using bearer to non-existing variant {}", request.getRequestURI());
				return null;
			}
			logger.debug("API request using bearer to exising variant id: {} API: {}", variant.getVariantID(), request.getRequestURI());
		}

		if (variant == null) {
			logger.warn("API request to non-existing variant {}", request.getRequestURI());
			return null;
		}

		Installation installation = clientInstallationService.findEnabledInstallationForVariantByDeviceToken(
				variant.getVariantID(), HttpBasicHelper.decodeBase64(deviceToken));
		// Installation should always be present.
		if (installation == null) {
			logger.info("API request to non-existing / disabled installation variant id: {} API: {}", variant.getVariantID(), request.getRequestURI());
			return null;
		}

		return variant;
	}

	/**
	 * returns application if the masterSecret is valid for the request
	 * PushApplicationEntity
	 */
	public static Variant loadVariantWhenAuthorized(GenericVariantService genericVariantService,
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

        logger.warn("UnAuthorized authentication using variantID: " + variantID + ", Secret: " + secret);
        // unauthorized...
        return null;
    }


	/**
	 * returns variant from the bearer token if it is valid for the request
	 */
	private static Variant loadVariantFromBearerWhenAuthorized(GenericVariantService genericVariantService,
			HttpServletRequest request) {
		// extract the pushApplicationID from the Authorization header:
		final Variant variant = BearerHelper.extractVariantFromBearerHeader(genericVariantService, request);

		if (variant != null) {
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
