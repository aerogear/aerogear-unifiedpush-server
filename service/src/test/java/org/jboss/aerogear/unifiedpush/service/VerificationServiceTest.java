package org.jboss.aerogear.unifiedpush.service;

import javax.validation.ConstraintValidator;

import org.jboss.aerogear.unifiedpush.api.validation.AlwaysTrueValidator;
import org.jboss.aerogear.unifiedpush.service.impl.VerificationGatewayServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.VerificationGatewayServiceImpl.VerificationPart;
import org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;
import org.jboss.aerogear.unifiedpush.service.sms.SendGridEmailSender;
import org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO - Convert into Spring based test.
 */
public class VerificationServiceTest {

	private VerificationGatewayServiceImpl vService;

	public void init() {
		vService = new VerificationGatewayServiceImpl();
		Configuration conf = new Configuration();
		conf.loadProperties();
		vService.setConfiguration(conf);
		vService.initializeSender();
	}

	@After
	public void after(){
		System.clearProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY);
	}

	@Test
	public void singleImplTest() {
		init();

		Assert.assertTrue(vService.getChain().size() != 0);
	}

	@Test
	public void singleImplWithDefaultType() {
		init();

		for (VerificationPart part : vService.getChain()) {
			Assert.assertTrue(AlwaysTrueValidator.class.isAssignableFrom(part.getValidator().getClass()));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" } )
	@Test
	public void singleImplWithType() {
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY, "org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender");

		init();

		for (VerificationPart part : vService.getChain()) {
			Assert.assertTrue(PhoneValidator.class.isAssignableFrom(part.getValidator().getClass()));
			Assert.assertTrue(ClickatellSMSSender.class.isAssignableFrom(part.getPublisher().getClass()));

			ConstraintValidator validator = part.getValidator();

			Assert.assertTrue(validator.isValid("+13216549877", null));
			Assert.assertTrue(validator.isValid("+0013216549877", null));
			Assert.assertTrue(validator.isValid("0013216549877", null));
			Assert.assertTrue(validator.isValid("443216549877", null));
			Assert.assertTrue(validator.isValid("3216549877", null));
			Assert.assertFalse(validator.isValid("xxx@test.com", null));
		}
	}


	@Test
	public void singleImplWithType1() {
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY, "org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;");

		init();

		for (VerificationPart part : vService.getChain()) {
			Assert.assertTrue(PhoneValidator.class.isAssignableFrom(part.getValidator().getClass()));
			Assert.assertTrue(ClickatellSMSSender.class.isAssignableFrom(part.getPublisher().getClass()));
		}
	}

	@Test
	public void multipleImplWithType() {
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY, "org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator::org.jboss.aerogear.unifiedpush.service.sms.SendGridEmailSender");

		init();

		Assert.assertTrue(vService.getChain().size() == 2);

		int counter = 0;

		for (VerificationPart part : vService.getChain()) {
			if (counter == 0) // First in order
				Assert.assertTrue(PhoneValidator.class.isAssignableFrom(part.getValidator().getClass()));

			if (counter == 1) // Second in order
				Assert.assertTrue(SendGridEmailSender.class.isAssignableFrom(part.getPublisher().getClass()));

			counter++;
		}
	}

}
