package org.jboss.aerogear.unifiedpush.service.impl;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationAsyncService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Async implementation wrapper.
 */
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class ClientInstallationAsyncServiceImpl implements ClientInstallationAsyncService {
	@Inject
	private ClientInstallationService clientInstallationService;

	@Async
	public void addInstallation(Variant variant, Installation installation, boolean shouldVerifiy) {
		clientInstallationService.addInstallation(variant, installation, shouldVerifiy);
	}

	@Async
	public void addInstallation(Variant variant, Installation installation) {
		clientInstallationService.addInstallation(variant, installation);
	}

	@Async
	public void addInstallations(Variant variant, List<Installation> installations) {
		clientInstallationService.addInstallations(variant, installations);
	}

	@Async
	public void removeInstallationsForVariantByDeviceTokens(String variantID, Set<String> deviceTokens) {
		clientInstallationService.removeInstallationsForVariantByDeviceTokens(variantID, deviceTokens);
	}

	@Async
	public void removeInstallationForVariantByDeviceToken(String variantID, String deviceToken) {
		clientInstallationService.removeInstallationForVariantByDeviceToken(variantID, deviceToken);
	}

}
