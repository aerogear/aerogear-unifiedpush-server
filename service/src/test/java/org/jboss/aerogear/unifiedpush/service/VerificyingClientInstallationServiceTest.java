package org.jboss.aerogear.unifiedpush.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.InstallationVerificationAttempt;
import org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.transaction.annotation.Transactional;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VerificyingClientInstallationServiceTest extends AbstractCassandraServiceTest {

	@Inject
	private VerificationService verificationService;

	@Inject
	private GenericVariantService variantService;

	@Inject
	private ClientInstallationService clientInstallationService;

	private AndroidVariant androidVariant;

	@Override
	protected void specificSetup() {
		// setup a variant:
		androidVariant = new AndroidVariant();
		androidVariant.setGoogleKey("Key");
		androidVariant.setName("Android");
		androidVariant.setDeveloper("me");
		variantService.addVariant(androidVariant, new LoggedInUser(DEFAULT_USER));
	}

	@Test
	@Transactional
	public void testSendCorrectVerificationCode() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(TestUtils.generateFakedDeviceTokenString());
		device.setVariant(androidVariant);

		clientInstallationService.addInstallation(androidVariant, device);

		String verificationCode = verificationService.initiateDeviceVerification(device, androidVariant);
		assertNotNull(verificationCode);

		// Clear local cache and force fetch from cassandra
		verificationService.clearCache();
		sleepSilently(100); // Wait for cassandra write.

		VerificationResult result = verificationService.verifyDevice(device, androidVariant,
				new InstallationVerificationAttempt(verificationCode, device.getDeviceToken()));
		assertEquals(VerificationResult.SUCCESS, result);
	}

	@Test
	@Transactional
	public void testSendCorrectVerificationCodeAndFakeDeviceToken() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(TestUtils.generateFakedDeviceTokenString());
		device.setVariant(androidVariant);
		device.setEnabled(false);
		clientInstallationService.addInstallation(androidVariant, device);

		String verificationCode = verificationService.initiateDeviceVerification(device, androidVariant);

		assertNotNull(verificationCode);

		Installation fakeDevice = new Installation();
		fakeDevice.setAlias("myalias");
		fakeDevice.setDeviceToken("fake device");
		fakeDevice.setVariant(androidVariant);
		fakeDevice.setEnabled(false);

		VerificationResult result = verificationService.verifyDevice(fakeDevice, androidVariant,
				new InstallationVerificationAttempt(verificationCode, device.getDeviceToken()));
		assertEquals(VerificationResult.FAIL, result);
	}

	@Test
	@Transactional
	public void testSendWrongVerificationCode() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(TestUtils.generateFakedDeviceTokenString());
		device.setVariant(androidVariant);
		device.setEnabled(false);
		clientInstallationService.addInstallation(androidVariant, device);

		String verificationCode = verificationService.initiateDeviceVerification(device, androidVariant);
		assertNotNull(verificationCode);

		VerificationResult result = verificationService.verifyDevice(device, androidVariant,
				new InstallationVerificationAttempt(verificationCode + "1", device.getDeviceToken()));
		assertEquals(VerificationResult.FAIL, result);

		// now retry with the correct code.
		result = verificationService.verifyDevice(device, androidVariant,
				new InstallationVerificationAttempt(verificationCode, device.getDeviceToken()));
		assertEquals(VerificationResult.SUCCESS, result);
	}

	@Test
	@Transactional
	public void testResendVerificationCode() {
		final String deviceToken = TestUtils.generateFakedDeviceTokenString();
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(deviceToken);
		device.setVariant(androidVariant);

		clientInstallationService.addInstallation(androidVariant, device);

		String verificationCode = verificationService.initiateDeviceVerification(device, androidVariant);
		assertNotNull(verificationCode);

		String newVerificationCode = verificationService.retryDeviceVerification(deviceToken, androidVariant);
		assertNotNull(newVerificationCode);

		// the first code should have been invalidated
		VerificationResult result = verificationService.verifyDevice(device, androidVariant,
				new InstallationVerificationAttempt(verificationCode, device.getDeviceToken()));
		assertEquals(VerificationResult.SUCCESS, result);

		result = verificationService.verifyDevice(device, androidVariant,
				new InstallationVerificationAttempt(newVerificationCode, device.getDeviceToken()));
		// Device was already enabled so return fail.
		assertEquals(VerificationResult.FAIL, result);
	}

}
