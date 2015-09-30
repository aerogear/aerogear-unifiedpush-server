package org.jboss.aerogear.unifiedpush.service.impl;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.Configuration;
import org.jboss.aerogear.unifiedpush.service.VerificationService;

/**
 * An alternative class which disables the installation prior to adding it. Once the installation is added,
 * this implementation fires a verification request to the newly added installation.
 */
@Stateless
@Alternative
public class VerifyingClientInstallationServiceImpl extends ClientInstallationServiceImpl implements ClientInstallationService {
	
	private static final String ENABLE_VERIFICATION = "aerogear.config.enable_sms_verification";
	
	@Inject
	private Configuration configuration;
	
	@Inject
	private VerificationService verificationService;
	
	// TODO: this logic might be worth moving to ClientInstallationServiceImpl itself (and then removing this class).
	// The decision whether to verify installations or not could be made part of the Application.
	@Override
    @Asynchronous
    public void addInstallation(Variant variant, Installation entity) {
		
		boolean shouldVerifiy = configuration.getBooleanProperty(ENABLE_VERIFICATION, false);
		
		if (shouldVerifiy) {
			entity.setEnabled(false);
			super.addInstallation(variant, entity);
			verificationService.initiateDeviceVerification(entity);
		} else {
			super.addInstallation(variant, entity);
		}
	}
}
