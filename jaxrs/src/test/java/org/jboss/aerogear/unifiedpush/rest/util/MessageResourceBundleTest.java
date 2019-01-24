package org.jboss.aerogear.unifiedpush.rest.util;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;
import org.jboss.aerogear.unifiedpush.service.sms.SendGridEmailSender;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.MessageSource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { WebConfigTest.class })
public class MessageResourceBundleTest extends RestEndpointTest {
	@Autowired
	protected MessageSource messageSource;

	@Test
	public void testInternalMessages() {
		assertEquals(messageSource != null, true);

		assertEquals(
				messageSource.getMessage(ClickatellSMSSender.MESSAGE_TMPL, new Object[] { "12345" }, Locale.ENGLISH),
				"Your CB4 verification code is: 12345");
		
		assertEquals(
				messageSource.getMessage(ClickatellSMSSender.MESSAGE_TMPL, new Object[] { "12345" }, Locale.FRANCE),
				"Votre code de vérification CB4 est: 12345");
		
		assertEquals(
				messageSource.getMessage(ClickatellSMSSender.MESSAGE_TMPL, new Object[] { "12345" }, new Locale("es", "ES")),
				"Su código de verificación para CB4 es: 12345");
		
		String test = messageSource.getMessage(SendGridEmailSender.MESSAGE_TMPL, new Object[] { "12345" }, Locale.ENGLISH);
		assertEquals(
				test,
				"Your verification code for the CB4 account is: <b>12345</b>. Please use this code to verify your device.<br/><br/>Thank you for using CB4.<br/>Sincerely,<br/>The CB4 Team");
	}
	

}
