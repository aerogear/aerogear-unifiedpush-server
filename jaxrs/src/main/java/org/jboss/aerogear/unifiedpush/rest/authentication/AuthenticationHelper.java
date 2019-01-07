package org.jboss.aerogear.unifiedpush.rest.authentication;

import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.util.BearerHelper;
import org.jboss.aerogear.unifiedpush.rest.util.ClientAuthHelper;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.rest.util.PushAppAuthHelper;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.KeycloakServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class AuthenticationHelper {
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationHelper.class);

	@Inject
	private ClientInstallationService clientInstallationService;
	@Inject
	private GenericVariantService genericVariantService;
	@Inject
	private PushApplicationService pushApplicationService;
	@Inject
	private AliasService aliasService;

	/**
	 * Returns the {@link PushApplication}. First try variant based authentication,
	 * then try application based authentication.
	 *
	 * @param request {@link HttpServletRequest}
	 * @param alias   For application level authentication, make sure alias is
	 *                associated.
	 */
	public PushApplication loadApplicationWhenAuthorized(HttpServletRequest request, String alias) {

		// Extract device token
		String deviceToken = ClientAuthHelper.getDeviceToken(request);

		// Try device based authentication
		if (StringUtils.isNotEmpty(deviceToken)) {
			final Variant variant = loadVariantWhenAuthorized(deviceToken, alias, request);

			if (variant == null) {
				return null;
			}

			// Find application by variant
			return pushApplicationService.findByVariantID(variant.getVariantID());
		}

		// Try application based authentication
		PushApplication pushApplication = PushAppAuthHelper.loadPushApplicationWhenAuthorized(request,
				pushApplicationService);

		if (pushApplication == null) {
			logger.warn("UnAuthorized application authentication attempt, credentials ({}) are not authorized",
					HttpBasicHelper.getAuthorizationHeader(request));
			return null;
		}

		// Application authentication can only access associated aliases !
		if (StringUtils.isNoneEmpty(alias)
				&& aliasService.find(pushApplication.getPushApplicationID(), alias) == null) {
			logger.warn(
					"UnAuthorized application authentication, application ({}) is not authorized for alias ({}) scope data !",
					pushApplication.getName(), alias);
			return null;
		}

		return pushApplication;
	}

	public PushApplication loadApplicationWhenAuthorized(HttpServletRequest request) {
		String applicationName = KeycloakServiceImpl.stripClientPrefix(BearerHelper.extractClientId(request));

		return pushApplicationService.findByName(applicationName);
	}

	/**
	 * Returns the {@link Installation} if device token exists and request is
	 * authenticated (Basic Or Bearer).
	 *
	 * @param request {@link HttpServletRequest}
	 *
	 */
	public Optional<Installation> loadInstallationWhenAuthorized(HttpServletRequest request) {
		// Extract device token
		String deviceToken = ClientAuthHelper.getDeviceToken(request);

		Variant variant = loadVariantWhenAuthorized(deviceToken, null, request);

		if (variant != null) {
			return Optional.ofNullable(clientInstallationService
					.findInstallationForVariantByDeviceToken(variant.getVariantID(), deviceToken));
		}

		return Optional.empty();
	}

	/**
	 * Returns the {@link Variant} if device token is present. first fetch
	 * credentials from basic authentication then try OAuth2.
	 *
	 * @param deviceToken Base64 decoded device token.
	 * @param alias       optional, validate alias to jwt.
	 * @param request     {@link HttpServletRequest}
	 */
	private Variant loadVariantWhenAuthorized(String deviceToken, String alias, HttpServletRequest request) {

		if (StringUtils.isEmpty(deviceToken)) {
			logger.warn("API request missing device-token header ({}), URI - > {}", deviceToken,
					request.getRequestURI());
			return null;
		}

		// Get variant from basic authentication headers
		Variant variant = ClientAuthHelper.loadVariantWhenAuthorized(genericVariantService, request, false);

		if (variant == null) {
			// Variant is missing, try to extract variant using Bearer
			if (BearerHelper.isBearerExists(request)) {
				variant = BearerHelper.extractVariantFromBearerHeader(pushApplicationService, alias, request);

				if (variant == null) {
					logger.info("UnAuthorized bearer authentication missing variant. device token ({}), URI {}",
							deviceToken, request.getRequestURI());
					return null;
				}
				logger.debug("Authorized bearer authentication to exising variant id: {} API: {}",
						variant.getVariantID(), request.getRequestURI());
			} else {
				// Variant is missing to anonymous/otp mode
				logger.warn("UnAuthorized basic authentication using token-id {} API: {}", deviceToken,
						request.getRequestURI());
				return null;
			}
		}

		return variant;
	}
}
