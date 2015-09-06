package org.jboss.aerogear.unifiedpush.service;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult;
import org.jboss.aerogear.unifiedpush.service.impl.MockSMSService;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

//@RunWith(MockitoJUnitRunner.class)
public class VerificyingClientInstallationServiceTest extends AbstractBaseServiceTest {

	@Inject
	private VerificationService verificationService;
	
	@Inject
	private MockSMSService smsService;
	
	@Override
	protected void specificSetup() {
		// no op
	}
	
	@Test
	public void testSendCorrectVerificationCode() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken("mytoken");
		verificationService.initiateDeviceVerification(device);
		String verificationCode = smsService.message;
		VerificationResult result = verificationService.verifyDevice(device, verificationCode);
		assertThat(result == VerificationResult.SUCCESS);
	}
	
	@Test
	public void testSendCorrectVerificationCodeAndFakeDeviceToken() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken("mytoken");
		verificationService.initiateDeviceVerification(device);
		String verificationCode = smsService.message;
		
		Installation fakeDevice = new Installation();
		fakeDevice.setAlias("myalias");
		fakeDevice.setDeviceToken("fake device");
		
		VerificationResult result = verificationService.verifyDevice(device, verificationCode);
		assertThat(result == VerificationResult.UNKNOWN);
	}
	
	@Test
	public void testSendWrongVerificationCode() {
		Installation device = new Installation();
		device.setAlias("myalias");
		device.setDeviceToken("mytoken");
		verificationService.initiateDeviceVerification(device);
		String verificationCode = smsService.message;
		VerificationResult result = verificationService.verifyDevice(device, verificationCode + "0");
		assertThat(result == VerificationResult.FAIL);
	}
	
}
