package org.jboss.aerogear.unifiedpush.service;

import java.lang.annotation.Annotation;

import javax.validation.ConstraintValidator;

import org.jboss.aerogear.unifiedpush.api.validation.AlwaysTrueValidator;
import org.jboss.aerogear.unifiedpush.service.impl.VerificationGatewayServiceImpl;
import org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;
import org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class VerificationServiceTest {

	private VerificationGatewayServiceImpl vService;

	public void init() {
		vService = new VerificationGatewayServiceImpl();
		Configuration conf = new Configuration();
		conf.loadProperties();
		conf.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
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

		Assert.assertTrue(vService.getPublishers().keySet().size() != 0);
	}

	@Test
	public void singleImplWithDefaultType() {
		init();

		for (ConstraintValidator<? extends Annotation, ?> validator : vService.getPublishers().keySet()) {
			Assert.assertTrue(AlwaysTrueValidator.class.isAssignableFrom(validator.getClass()));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" } )
	@Test
	public void singleImplWithType() {
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY, "org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender");

		init();

		for (ConstraintValidator validator : vService.getPublishers().keySet()) {
			Assert.assertTrue(PhoneValidator.class.isAssignableFrom(validator.getClass()));
			Assert.assertTrue(ClickatellSMSSender.class.isAssignableFrom(vService.getPublishers().get(validator).getClass()));


			Assert.assertTrue(validator.isValid("+13216549877", null));
			Assert.assertTrue(validator.isValid("+0013216549877", null));
			Assert.assertTrue(validator.isValid("0013216549877", null));
			Assert.assertTrue(validator.isValid("443216549877", null));
			Assert.assertTrue(validator.isValid("3216549877", null));
			Assert.assertFalse(validator.isValid("xxx@test.com", null));
		}
	}


	@SuppressWarnings("rawtypes" )
	@Test
	public void singleImplWithType1() {
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY, "org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;");

		init();

		for (ConstraintValidator validator : vService.getPublishers().keySet()) {
			Assert.assertTrue(PhoneValidator.class.isAssignableFrom(validator.getClass()));
			Assert.assertTrue(ClickatellSMSSender.class.isAssignableFrom(vService.getPublishers().get(validator).getClass()));
		}
	}

	@SuppressWarnings("rawtypes" )
	@Test
	public void multipleImplWithType() {
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY, "org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender");

		init();

		Assert.assertTrue(vService.getPublishers().size() == 2);

		for (ConstraintValidator validator : vService.getPublishers().keySet()) {
			Assert.assertTrue(PhoneValidator.class.isAssignableFrom(validator.getClass()));
			Assert.assertTrue(ClickatellSMSSender.class.isAssignableFrom(vService.getPublishers().get(validator).getClass()));
		}
	}

}
