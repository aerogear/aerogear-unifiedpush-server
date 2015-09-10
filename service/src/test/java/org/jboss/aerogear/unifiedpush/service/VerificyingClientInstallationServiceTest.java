package org.jboss.aerogear.unifiedpush.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult;
import org.jboss.aerogear.unifiedpush.service.impl.MockSMSService;
import org.junit.Test;

//@RunWith(MockitoJUnitRunner.class)
public class VerificyingClientInstallationServiceTest extends AbstractBaseServiceTest {

	@Inject
	private VerificationService verificationService;
	
	@Inject
	private MockSMSService smsService;
	
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
	public void testSendCorrectVerificationCode() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(TestUtils.generateFakedDeviceTokenString());
		device.setVariant(androidVariant);
		clientInstallationService.addInstallation(androidVariant, device);

		verificationService.initiateDeviceVerification(device);
		String verificationCode = smsService.phoneToMessage.get("myalias");
		
		assertNotNull(verificationCode);
		
		VerificationResult result = verificationService.verifyDevice(androidVariant.getVariantID(), device.getDeviceToken(), verificationCode);
		assertEquals(VerificationResult.SUCCESS, result);
	}
	
	@Test
	public void testSendCorrectVerificationCodeAndFakeDeviceToken() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(TestUtils.generateFakedDeviceTokenString());
		device.setVariant(androidVariant);
		clientInstallationService.addInstallation(androidVariant, device);
		
		verificationService.initiateDeviceVerification(device);
		String verificationCode = smsService.phoneToMessage.get("myalias");
		
		assertNotNull(verificationCode);
		
		Installation fakeDevice = new Installation();
		fakeDevice.setAlias("myalias");
		fakeDevice.setDeviceToken("fake device");
		
		VerificationResult result = verificationService.verifyDevice(androidVariant.getVariantID(), fakeDevice.getDeviceToken(), verificationCode);
		assertEquals(VerificationResult.UNKNOWN, result);	
	}
	
	@Test
	public void testSendWrongVerificationCode() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken(TestUtils.generateFakedDeviceTokenString());
		device.setVariant(androidVariant);
		clientInstallationService.addInstallation(androidVariant, device);

		verificationService.initiateDeviceVerification(device);
		String verificationCode = smsService.phoneToMessage.get("myalias");
		assertNotNull(verificationCode);
		VerificationResult result = verificationService.verifyDevice(androidVariant.getVariantID(), device.getDeviceToken(), verificationCode + "1");
		assertEquals(VerificationResult.FAIL, result);
	}
	
}
