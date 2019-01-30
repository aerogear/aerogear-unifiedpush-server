package org.jboss.aerogear.unifiedpush.rest.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.aerogear.unifiedpush.service.sms.ClickatellSMSSender;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.MessageSource;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { WebConfigTest.class })
public class MessageResourceBundleExternalTest extends RestEndpointTest {
	@Autowired
	protected MessageSource messageSource;

	static {
		String dirPath = System.getProperty("java.io.tmpdir");

		List<String> lines = new ArrayList<>();
		lines.add("aerogear.config.sms.sender.clickatell.template=XXX Votre code de vérification CB4 est: {0}");

		try {
			Files.deleteIfExists(Paths.get(dirPath + File.separator + "messages_fr.properties"));

			Files.createDirectories(Paths.get(dirPath));
			Files.write(Paths.get(dirPath + File.separator + "messages_fr.properties"), lines,
					StandardOpenOption.CREATE_NEW);
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.setProperty(ConfigurationEnvironment.CONF_DIR, dirPath);
	}

	@Test
	public void testMessages() {
		assertEquals(messageSource != null, true);

		// Internal prop
		assertEquals(
				messageSource.getMessage(ClickatellSMSSender.MESSAGE_TMPL, new Object[] { "12345" }, Locale.ENGLISH),
				"Your CB4 verification code is: 12345");
		// External prop
		assertEquals(
				messageSource.getMessage(ClickatellSMSSender.MESSAGE_TMPL, new Object[] { "123" }, Locale.FRANCE),
				"XXX Votre code de vérification CB4 est: 123");
	}

}
