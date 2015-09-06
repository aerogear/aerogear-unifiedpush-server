package org.jboss.aerogear.unifiedpush.service.impl;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.VerificationService;

/**
 * An alternative class which disables the installation prior to adding it. Once the installation is added,
 * this implementation fires a verification request to the newly added installation.
 */
@Stateless
@Alternative
public class VerifyingClientInstallationServiceImpl extends ClientInstallationServiceImpl implements ClientInstallationService {
	
	@Inject
	private VerificationService verificationService;
	
	// TODO: this logic might be worth moving to ClientInstallationServiceImpl itself (and then removing this class).
	// The decision whether to verify installations or not could be made part of the Application.
	@Override
    @Asynchronous
    public void addInstallation(Variant variant, Installation entity) {
		entity.setEnabled(false);
		super.addInstallation(variant, entity);
		verificationService.initiateDeviceVerification(entity);
	}
}
