package org.jboss.aerogear.unifiedpush.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.junit.Assert;
import org.junit.Test;

public class DocumentServiceTest extends AbstractBaseServiceTest {
	private static final String DEFAULT_DEVICE_TOKEN = "c5106a4e97ecc8b8ab8448c2ebccbfa25938c0f9a631f96eb2dd5f16f0bedc40";
	private static final String DEFAULT_DEVICE_ALIAS = "17327572923";
	private static final String DEFAULT_DEVICE_QUALIFIER = "TEST";

	private static final String DEFAULT_VARIENT_ID = "d3f54c25-c3ce-4999-b7a8-27dc9bb01364";

	@Inject
	private DocumentService documentService;
	@Inject
	private Configuration configuration;
	@Inject
	private GenericVariantService genericVariantService;
	@Inject
	private ClientInstallationService installationService;
	@Inject
	private PushApplicationService applicationService;
	@Inject
	private VerificationService verificationService;

	@Override
	protected void specificSetup() {
		// Clean old documents
		String pathRoot = configuration.getProperty(Configuration.PROPERTIES_DOCUMENTS_KEY);
		try {
			FileUtils.deleteDirectory(new File(pathRoot));
		} catch (IOException e) {
			// Fail to delete.
		}
	}

	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void saveDocumentTest() {
		// Prepare installation
		Installation iosInstallation = new Installation();
		iosInstallation.setDeviceType("iPhone7,2");
		iosInstallation.setDeviceToken(DEFAULT_DEVICE_TOKEN);
		iosInstallation.setOperatingSystem("iOS");
		iosInstallation.setOsVersion("9.0.2");
		iosInstallation.setAlias(DEFAULT_DEVICE_ALIAS);

		try {
			configuration.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
			System.setProperty(Configuration.PROP_ENABLE_VERIFICATION, "true");

			Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
			Assert.assertTrue(variant.getVariantID().equals(DEFAULT_VARIENT_ID));

			installationService.addInstallationSynchronously(variant, iosInstallation);

			Installation inst = installationService.findById(iosInstallation.getId());
			Assert.assertTrue(inst != null && inst.isEnabled() == false);

			// Register alias
			PushApplication pushApplication = applicationService.findByVariantID(variant.getVariantID());

			Map<String, List<String>> aliasToDocuments = new HashMap<String, List<String>>();
			List<String> documents = new ArrayList<String>();
			documents.add("{TEST JSON}");
			aliasToDocuments.put(DEFAULT_DEVICE_ALIAS, documents);

			// Save alias should return without saving, divice is not emabled.
			documentService.saveForAliases(pushApplication, aliasToDocuments, DEFAULT_DEVICE_QUALIFIER);
			documents = documentService.getAliasDocuments(variant, DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_QUALIFIER,
					new Date(System.currentTimeMillis() - 5000));

			// Enable device
			String code = verificationService.initiateDeviceVerification(inst, variant);
			VerificationResult results = verificationService.verifyDevice(inst, variant, code);
			Assert.assertTrue(results != null && results.equals(VerificationResult.SUCCESS));

			// Re-save device
			documentService.saveForAliases(pushApplication, aliasToDocuments, DEFAULT_DEVICE_QUALIFIER);
			documents = documentService.getAliasDocuments(variant, DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_QUALIFIER,
					new Date(System.currentTimeMillis() - 5000));

			Assert.assertTrue(documents != null && documents.size() == 1);
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		} finally {
			// Rest system property to false
			System.setProperty(Configuration.PROP_ENABLE_VERIFICATION, "false");
		}
	}
	
	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void saveDocumentWithoutVerificationModeTest() {
		// Prepare installation
		Installation iosInstallation = new Installation();
		iosInstallation.setDeviceType("iPhone7,2");
		iosInstallation.setDeviceToken(DEFAULT_DEVICE_TOKEN);
		iosInstallation.setOperatingSystem("iOS");
		iosInstallation.setOsVersion("9.0.2");
		iosInstallation.setAlias(DEFAULT_DEVICE_ALIAS);

		Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
		Assert.assertTrue(variant.getVariantID().equals(DEFAULT_VARIENT_ID));

		installationService.addInstallationSynchronously(variant, iosInstallation);

		Installation inst = installationService.findById(iosInstallation.getId());
		Assert.assertTrue(inst != null && inst.isEnabled() == true);

		// Register alias
		PushApplication pushApplication = applicationService.findByVariantID(variant.getVariantID());

		Map<String, List<String>> aliasToDocuments = new HashMap<String, List<String>>();
		List<String> documents = new ArrayList<String>();
		documents.add("{TEST JSON}");
		aliasToDocuments.put(DEFAULT_DEVICE_ALIAS, documents);

		// Save alias should return without saving, divice is not emabled.
		documentService.saveForAliases(pushApplication, aliasToDocuments, DEFAULT_DEVICE_QUALIFIER);
		documents = documentService.getAliasDocuments(variant, DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_QUALIFIER,
				new Date(System.currentTimeMillis() - 5000));
		
		Assert.assertTrue(documents != null && documents.size() == 1);
	}
}
