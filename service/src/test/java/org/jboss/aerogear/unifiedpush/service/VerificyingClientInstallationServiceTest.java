package org.jboss.aerogear.unifiedpush.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Test;

//@RunWith(MockitoJUnitRunner.class)
public class VerificyingClientInstallationServiceTest extends AbstractBaseServiceTest {

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
        variantService.addVariant(androidVariant);
    }
   
	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void testSendCorrectVerificationCode() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(TestUtils.generateFakedDeviceTokenString());
		device.setVariant(androidVariant);
		
		clientInstallationService.addInstallationSynchronously(androidVariant, device);
		
		String verificationCode = verificationService.initiateDeviceVerification(device, androidVariant);
		assertNotNull(verificationCode);
		
		VerificationResult result = verificationService.verifyDevice(androidVariant.getVariantID(), device.getDeviceToken(), verificationCode);
		assertEquals(VerificationResult.SUCCESS, result);
	}
	
	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void testSendCorrectVerificationCodeAndFakeDeviceToken() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(TestUtils.generateFakedDeviceTokenString());
		device.setVariant(androidVariant);
		clientInstallationService.addInstallationSynchronously(androidVariant, device);
		
		String verificationCode = verificationService.initiateDeviceVerification(device, androidVariant); 
		
		assertNotNull(verificationCode);
		
		Installation fakeDevice = new Installation();
		fakeDevice.setAlias("myalias");
		fakeDevice.setDeviceToken("fake device");
		
		VerificationResult result = verificationService.verifyDevice(androidVariant.getVariantID(), fakeDevice.getDeviceToken(), verificationCode);
		assertEquals(VerificationResult.UNKNOWN, result);	
	}
	
	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void testSendWrongVerificationCode() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(TestUtils.generateFakedDeviceTokenString());
		device.setVariant(androidVariant);
		clientInstallationService.addInstallationSynchronously(androidVariant, device);

		
		String verificationCode = verificationService.initiateDeviceVerification(device, androidVariant);
		assertNotNull(verificationCode);
		
		VerificationResult result = verificationService.verifyDevice(androidVariant.getVariantID(), device.getDeviceToken(), verificationCode + "1");
		assertEquals(VerificationResult.FAIL, result);
		
		// now retry with the correct code.
		result = verificationService.verifyDevice(androidVariant.getVariantID(), device.getDeviceToken(), verificationCode);
		assertEquals(VerificationResult.SUCCESS, result);
	}
	
	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void testResendVerificationCode() {
		final String deviceToken = TestUtils.generateFakedDeviceTokenString();
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(deviceToken);
		device.setVariant(androidVariant);
		clientInstallationService.addInstallationSynchronously(androidVariant, device);
		
		String verificationCode = verificationService.initiateDeviceVerification(device, androidVariant);
		assertNotNull(verificationCode);
		
		String newVerificationCode = verificationService.retryDeviceVerification(deviceToken, androidVariant);
		assertNotNull(newVerificationCode);
		
		// the first code should have been invalidated
		VerificationResult result = verificationService.verifyDevice(androidVariant.getVariantID(), device.getDeviceToken(), verificationCode);
		assertEquals(VerificationResult.SUCCESS, result);

		result = verificationService.verifyDevice(androidVariant.getVariantID(), device.getDeviceToken(), newVerificationCode);
		assertEquals(VerificationResult.UNKNOWN, result);
	}
	
}
