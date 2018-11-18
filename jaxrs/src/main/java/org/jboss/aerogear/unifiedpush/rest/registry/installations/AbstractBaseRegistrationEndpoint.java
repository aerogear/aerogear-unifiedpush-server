package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.validation.DeviceTokenValidator;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.authentication.AuthenticationHelper;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationAsyncService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;

public abstract class AbstractBaseRegistrationEndpoint extends AbstractBaseEndpoint {
	@Inject
	protected ClientInstallationService clientInstallationService;
	@Inject
	protected ClientInstallationAsyncService clientInstallationAsyncService;
	@Inject
	protected AuthenticationHelper authenticationHelper;
	
	protected Response register(Variant variant, final String oldToken, Installation entity, boolean synchronously,
			HttpServletRequest request) {
		// Poor up-front validation for required token
		final String deviceToken = entity.getDeviceToken();
		if (deviceToken == null || !DeviceTokenValidator.isValidDeviceTokenForVariant(deviceToken, variant.getType())) {
			logger.trace(String.format("Invalid device token was delivered: %s for variant type: %s", deviceToken,
					variant.getType()));
			return appendAllowOriginHeader(Response.status(Status.BAD_REQUEST), request);
		}

		// The 'mobile application' on the device/client was launched.
		// If the installation is already in the DB, let's update the metadata,
		// otherwise we register a new installation:
		logger.trace("Mobile Application on device was launched");

		// The token has changed, remove the old one
		if (!oldToken.isEmpty() && !oldToken.equals(entity.getDeviceToken())) {
			logger.info(String.format("Deleting old device token %s", oldToken));
			clientInstallationAsyncService.removeInstallationForVariantByDeviceToken(variant.getVariantID(), oldToken);
		}

		// In some cases (automation ), we need to
		// make sure device is synchronously registered.
		if (synchronously)
			clientInstallationService.addInstallation(variant, entity);
		else
			clientInstallationAsyncService.addInstallation(variant, entity);

		return appendAllowOriginHeader(Response.ok(entity), request);
	}

}
