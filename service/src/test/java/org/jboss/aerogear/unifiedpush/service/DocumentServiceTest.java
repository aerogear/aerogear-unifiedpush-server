package org.jboss.aerogear.unifiedpush.service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.DocumentMetadata.DocumentType;
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
			PushApplication pushApplication = applicationService.findByVariantID(DEFAULT_VARIENT_ID);

			// Save alias should return without saving, device is not enabled.
			documentService.savePayload(pushApplication, DEFAULT_DEVICE_ALIAS, "{TEST JSON}" , DEFAULT_DEVICE_QUALIFIER, null, false);
			String document = documentService.getLatestDocumentForAlias(variant, DocumentType.APPLICATION,
					DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_QUALIFIER);

			// Enable device
			String code = verificationService.initiateDeviceVerification(inst, variant);
			VerificationResult results = verificationService.verifyDevice(inst, variant, code);
			Assert.assertTrue(results != null && results.equals(VerificationResult.SUCCESS));

			// Re-save device
			documentService.savePayload(pushApplication, DEFAULT_DEVICE_ALIAS, "{TEST JSON}", DEFAULT_DEVICE_QUALIFIER, null, false);
			document = documentService.getLatestDocumentForAlias(variant, DocumentType.APPLICATION, DEFAULT_DEVICE_ALIAS,
					DEFAULT_DEVICE_QUALIFIER);

			Assert.assertTrue(document != null && document.equals("{TEST JSON}"));
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		} finally {
			// Rest system property to false
			System.setProperty(Configuration.PROP_ENABLE_VERIFICATION, "false");
		}
	}

	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void lastUpdatedDocumentTest() {
		saveDocumentTest();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Nothing to do
		}

		Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
		PushApplication pushApplication = applicationService.findByVariantID(DEFAULT_VARIENT_ID);

		// Save alias should return without saving, device is not enabled.
		documentService.savePayload(pushApplication,  DEFAULT_DEVICE_ALIAS, "{TEST JSON NEWEST}", DEFAULT_DEVICE_QUALIFIER, null, false);
		String document = documentService.getLatestDocumentForAlias(variant, DocumentType.APPLICATION, DEFAULT_DEVICE_ALIAS,
				DEFAULT_DEVICE_QUALIFIER);

		Assert.assertTrue(document != null && document.equals("{TEST JSON NEWEST}"));
	}

	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void saveGlobalDocumentTest() {
		Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
		PushApplication pushApplication = applicationService.findByVariantID(DEFAULT_VARIENT_ID);

		// Save alias should return without saving, device is not enabled.
		documentService.savePayload(pushApplication,  DocumentMetadata.NULL_ALIAS, "{TEST JSON NULL_ALIAS}", DEFAULT_DEVICE_QUALIFIER, null, false);
		String document = documentService.getLatestDocumentForAlias(variant, DocumentType.APPLICATION, DocumentMetadata.NULL_ALIAS,
				DEFAULT_DEVICE_QUALIFIER);

		Assert.assertTrue(document != null && document.equals("{TEST JSON NULL_ALIAS}"));
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

		// Save alias should return without saving, divice is not emabled.
		documentService.savePayload(pushApplication, DEFAULT_DEVICE_ALIAS, "{TEST JSON}", DEFAULT_DEVICE_QUALIFIER, null, false);
		String document = documentService.getLatestDocumentForAlias(variant, DocumentType.APPLICATION,
				DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_QUALIFIER);

		Assert.assertTrue(document != null && document.equals("{TEST JSON}"));
	}

	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void saveDocumentOverwriteTest() {
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
		Assert.assertTrue(inst != null && inst.isEnabled());

		// Register alias
		PushApplication pushApplication = applicationService.findByVariantID(variant.getVariantID());

		Map<String, String> aliasToDocuments = new HashMap<String, String>();
		aliasToDocuments.put(DEFAULT_DEVICE_ALIAS, "{TEST JSON}");

		// Save once
		documentService.savePayload(pushApplication, DEFAULT_DEVICE_ALIAS, "{TEST JSON}", DEFAULT_DEVICE_QUALIFIER, null, true);
		String document = documentService.getLatestDocumentForAlias(variant, DocumentType.APPLICATION,
				DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_QUALIFIER);

		Assert.assertTrue(document != null && document.equals("{TEST JSON}"));

		aliasToDocuments.put(DEFAULT_DEVICE_ALIAS, "{TEST JSON 2}");

		// save 2nd time and check that it was overwritten
		documentService.savePayload(pushApplication, DEFAULT_DEVICE_ALIAS, "{TEST JSON 2}", DEFAULT_DEVICE_QUALIFIER, null, true);
		document = documentService.getLatestDocumentForAlias(variant, DocumentType.APPLICATION,
				DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_QUALIFIER);

		Assert.assertTrue(document != null && document.equals("{TEST JSON 2}"));
	}

	@Test
	@Transactional(TransactionMode.ROLLBACK)
	public void testFindLatestDocumentsForApplication() {
		Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
		PushApplication pushApp = applicationService.findByVariantID(variant.getVariantID());

		String alias1 = "alias1";
		String alias2 = "alias2";

		documentService.saveForPushApplication(pushApp, alias1, "doc1", DEFAULT_DEVICE_QUALIFIER, "test_id", true);
		documentService.saveForPushApplication(pushApp, alias2, "doc2", DEFAULT_DEVICE_QUALIFIER, "test_id", true);

		List<String> docs = documentService.getLatestDocumentsForApplication(pushApp, DEFAULT_DEVICE_QUALIFIER, "test_id");
		Assert.assertEquals(new HashSet<>(docs), new HashSet<>(Arrays.asList("doc1", "doc2")));
	}
}
