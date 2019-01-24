package org.jboss.aerogear.unifiedpush.service.spring;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.validation.ConstraintValidator;

import org.apache.commons.io.FileUtils;
import org.jboss.aerogear.unifiedpush.api.validation.AlwaysTrueValidator;
import org.jboss.aerogear.unifiedpush.service.AbstractNoCassandraServiceTest;
import org.jboss.aerogear.unifiedpush.service.VerificationPublisher.MessageType;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IVerificationGatewayService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.VerificationGatewayServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.spring.VerificationGatewayServiceImpl.VerificationPart;
import org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;
import org.jboss.aerogear.unifiedpush.service.sms.HtmlFileSender;
import org.jboss.aerogear.unifiedpush.service.sms.SendGridEmailSender;
import org.jboss.aerogear.unifiedpush.service.validation.ApplicationValidator;
import org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DelegatingMessageSource;
import org.springframework.context.support.StaticMessageSource;

public class VerificationServiceTest extends AbstractNoCassandraServiceTest {
	@Autowired
	private IVerificationGatewayService vService;
	@Autowired
	private DelegatingMessageSource messageSource;

	@After
	public void after() {
		System.clearProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY);
	}

	@Test
	public void singleImplTest() {
		Assert.assertTrue(vService.getChain().size() != 0);
	}

	@Test
	public void singleImplWithDefaultType() {
		vService.initializeSender();

		for (VerificationPart part : vService.getChain()) {
			Assert.assertTrue(AlwaysTrueValidator.class.isAssignableFrom(part.getValidator().getClass()));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void singleImplWithType() {
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY,
				"org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender");

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
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY,
				"org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;");
		vService.initializeSender();

		for (VerificationPart part : vService.getChain()) {
			Assert.assertTrue(PhoneValidator.class.isAssignableFrom(part.getValidator().getClass()));
			Assert.assertTrue(ClickatellSMSSender.class.isAssignableFrom(part.getPublisher().getClass()));
		}
	}

	@Test
	public void multipleImplWithType() {
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY,
				"org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator::org.jboss.aerogear.unifiedpush.service.sms.SendGridEmailSender");
		vService.initializeSender();

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

	@Test
	public void applicationSpecificConfig() {
		System.setProperty(VerificationGatewayServiceImpl.VERIFICATION_IMPL_KEY,
				"org.jboss.aerogear.unifiedpush.service.validation.ApplicationValidator::org.jboss.aerogear.unifiedpush.service.sms.HtmlFileSender;"
						+ "org.jboss.aerogear.unifiedpush.service.validation.PhoneValidator::org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;"
						+ "org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator::org.jboss.aerogear.unifiedpush.service.sms.SendGridEmailSender");
		System.setProperty(ApplicationValidator.APP_VALIDATION_KEY, "231231231");
		System.setProperty(HtmlFileSender.HTMLFILE_KEY, "/tmp/otp.html");
		StaticMessageSource messages = new StaticMessageSource();
		messages.addMessage(HtmlFileSender.MESSAGE_TMPL, Locale.ENGLISH, "{1} - Your CB4 verification code is: {0}");
		messageSource.setParentMessageSource(messages);
		vService.initializeSender();

		Assert.assertTrue(vService.getChain().size() == 3);

		vService.sendVerificationMessage("231231231", "test@ups.com", MessageType.REGISTER, "12345", Locale.ENGLISH);

		List<String> lines;
		try {
			lines = FileUtils.readLines(new File("/tmp/otp.html"), "UTF-8");
			Assert.assertTrue(lines.get(lines.size() - 1).contains("12345"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
