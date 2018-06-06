package org.jboss.aerogear.unifiedpush.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.InstallationVerificationAttempt;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.document.DocumentMetadata;
import org.jboss.aerogear.unifiedpush.api.document.QueryOptions;
import org.jboss.aerogear.unifiedpush.cassandra.dao.NullAlias;
import org.jboss.aerogear.unifiedpush.cassandra.dao.impl.DocumentKey;
import org.jboss.aerogear.unifiedpush.cassandra.dao.model.DocumentContent;
import org.jboss.aerogear.unifiedpush.message.Criteria;
import org.jboss.aerogear.unifiedpush.message.UnifiedPushMessage;
import org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IConfigurationService;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import com.datastax.driver.core.utils.UUIDs;

public class DocumentServiceTest extends AbstractCassandraServiceTest {
	private static final String DEFAULT_DEVICE_TOKEN = "c5106a4e97ecc8b8ab8448c2ebccbfa25938c0f9a631f96eb2dd5f16f0bedc40";
	private static final String DEFAULT_DEVICE_ALIAS = "17327572923";
	private static final String DEFAULT_DEVICE_DATABASE = "TEST";

	private static final String DEFAULT_VARIENT_ID = "d3f54c25-c3ce-4999-b7a8-27dc9bb01364";

	@Inject
	private DocumentService documentService;
	@Inject
	private GenericVariantService genericVariantService;
	@Inject
	private ClientInstallationService installationService;
	@Inject
	private PushApplicationService applicationService;
	@Inject
	private VerificationService verificationService;
	@Inject
	private AliasService aliasService;
	@Inject
	private IConfigurationService configuration;

	@Before
	public void cleanup() {
		System.clearProperty(ConfigurationEnvironment.PROP_ENABLE_VERIFICATION);
	}

	@Override
	protected void specificSetup() {

	}

	@Test
	@Transactional
	public void saveDocumentTest() {
		System.setProperty(ConfigurationEnvironment.PROP_ENABLE_VERIFICATION, "true");

		// Prepare installation
		Installation iosInstallation = new Installation();
		iosInstallation.setDeviceType("iPhone7,2");
		iosInstallation.setDeviceToken(DEFAULT_DEVICE_TOKEN);
		iosInstallation.setOperatingSystem("iOS");
		iosInstallation.setOsVersion("9.0.2");
		iosInstallation.setAlias(DEFAULT_DEVICE_ALIAS);

		try {
			Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
			Assert.assertTrue(variant.getVariantID().equals(DEFAULT_VARIENT_ID));

			installationService.addInstallation(variant, iosInstallation);

			Installation inst = installationService.findById(iosInstallation.getId());
			Assert.assertTrue(inst != null && inst.isEnabled() == true);

			// Register alias
			PushApplication pushApplication = applicationService.findByVariantID(DEFAULT_VARIENT_ID);
			DocumentMetadata metadata = getMetadata(pushApplication, DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_DATABASE);

			// Save once
			documentService.save(metadata, "{TEST JSON}", null);
			DocumentContent document = documentService.findLatest(metadata, null);

			// Enable device
			String code = verificationService.initiateDeviceVerification(inst, variant);
			VerificationResult results = verificationService.verifyDevice(inst, variant,
					new InstallationVerificationAttempt(code, inst.getDeviceToken()));
			Assert.assertTrue(results != null && results.equals(VerificationResult.SUCCESS));

			// Re-save device
			documentService.save(metadata, "{TEST JSON}", null);
			document = documentService.findLatest(metadata, null);

			Assert.assertTrue(document != null && document.getContent().equals("{TEST JSON}"));
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	@Transactional
	public void lastUpdatedDocumentTest() {
		saveDocumentTest();

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Nothing to do
		}

		// Register alias
		PushApplication pushApplication = applicationService.findByVariantID(DEFAULT_VARIENT_ID);
		DocumentMetadata metadata = getMetadata(pushApplication, DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_DATABASE);

		// Save alias should return without saving, device is not enabled.
		documentService.save(metadata, "{TEST JSON NEWEST}", null);

		DocumentContent document = documentService.findLatest(metadata, null);

		Assert.assertTrue(document != null && document.getContent().equals("{TEST JSON NEWEST}"));
	}

	@Test
	@Transactional
	public void saveGlobalDocumentTest() {
		// Register alias
		PushApplication pushApplication = applicationService.findByVariantID(DEFAULT_VARIENT_ID);

		DocumentMetadata metadata = new DocumentMetadata(pushApplication.getPushApplicationID(),
				DEFAULT_DEVICE_DATABASE, NullAlias.getAlias(pushApplication.getPushApplicationID()));

		// Save alias should return without saving, device is not enabled.
		documentService.save(metadata, "{TEST JSON NULL_ALIAS}", null);

		DocumentContent document = documentService.findLatest(
				getMetadata(pushApplication, DocumentMetadata.NULL_ALIAS.toString(), DEFAULT_DEVICE_DATABASE), null);

		Assert.assertTrue(document != null && document.getContent().equals("{TEST JSON NULL_ALIAS}"));
	}

	@Test
	@Transactional
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

		installationService.addInstallation(variant, iosInstallation);

		Installation inst = installationService.findById(iosInstallation.getId());
		Assert.assertTrue(inst != null && inst.isEnabled() == true);

		// Register alias
		PushApplication pushApplication = applicationService.findByVariantID(variant.getVariantID());
		Alias alias1 = new Alias(UUID.fromString(pushApplication.getPushApplicationID()), UUIDs.timeBased(),
				DEFAULT_DEVICE_ALIAS);
		aliasService.create(alias1);

		DocumentMetadata metadata = new DocumentMetadata(pushApplication.getPushApplicationID(),
				DEFAULT_DEVICE_DATABASE, alias1);

		documentService.save(metadata, "{TEST JSON}", null);

		DocumentContent document = documentService
				.findLatest(getMetadata(pushApplication, DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_DATABASE), null);

		Assert.assertTrue(document != null && document.getContent().equals("{TEST JSON}"));
	}

	@Test
	@Transactional
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

		installationService.addInstallation(variant, iosInstallation);

		Installation inst = installationService.findById(iosInstallation.getId());
		Assert.assertTrue(inst != null && inst.isEnabled());

		// Register alias
		PushApplication pushApplication = applicationService.findByVariantID(variant.getVariantID());

		DocumentMetadata metadata = getMetadata(pushApplication, DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_DATABASE);
		// Save once
		documentService.save(metadata, "{TEST JSON}", null);
		DocumentContent document = documentService
				.findLatest(getMetadata(pushApplication, DEFAULT_DEVICE_ALIAS, DEFAULT_DEVICE_DATABASE), null);

		Assert.assertTrue(document != null && document.getContent().equals("{TEST JSON}"));

		// save 2nd time and check that it was overwritten
		documentService.save(metadata, "{TEST JSON 2}", null);
		document = documentService.findLatest(metadata, null);

		Assert.assertTrue(document != null && document.getContent().equals("{TEST JSON 2}"));
	}

	@Test
	@Transactional
	public void testFindLatestDocumentsForApplication() {
		Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
		PushApplication pushApp = applicationService.findByVariantID(variant.getVariantID());

		Alias alias1 = new Alias(UUID.fromString(pushApp.getPushApplicationID()), UUIDs.timeBased(), "alias1");
		Alias alias2 = new Alias(UUID.fromString(pushApp.getPushApplicationID()), UUIDs.timeBased(), "alias2");

		aliasService.create(alias1);
		aliasService.create(alias2);

		documentService.save(new DocumentMetadata(pushApp.getPushApplicationID(), DEFAULT_DEVICE_DATABASE, alias1),
				"doc1", "test_id");
		documentService.save(new DocumentMetadata(pushApp.getPushApplicationID(), DEFAULT_DEVICE_DATABASE, alias2),
				"doc2", "test_id");

		List<DocumentContent> docs = documentService.findLatest(pushApp, DEFAULT_DEVICE_DATABASE, "test_id",
				Arrays.asList(alias1, alias2));
		Assert.assertEquals(Arrays.asList(docs.get(0).getContent(), docs.get(1).getContent()),
				Arrays.asList("doc1", "doc2"));
	}

	@Test
	@Transactional
	public void testNullAliasDocument() {
		Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
		PushApplication pushApplication = applicationService.findByVariantID(variant.getVariantID());

		UnifiedPushMessage message = new UnifiedPushMessage();
		message.setCriteria(new Criteria());
		message.getCriteria().setAliases(new ArrayList<>());
		message.getCriteria().getAliases().add(null);

		DocumentMetadata metadata = getMetadata(pushApplication, null, DocumentMetadata.NULL_DATABASE);
		// Save
		documentService.save(metadata, "{TEST PAYLOAD}", null);

		DocumentContent latest = documentService.findLatest(metadata, null);
		Assert.assertTrue(latest != null && latest.getContent().equals("{TEST PAYLOAD}"));
	}

	@Test
	@Transactional
	public void testNullDocumentIds() {
		Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
		PushApplication pushApplication = applicationService.findByVariantID(variant.getVariantID());

		UnifiedPushMessage message = new UnifiedPushMessage();
		message.setCriteria(new Criteria());
		message.getCriteria().setAliases(new ArrayList<>());
		message.getCriteria().getAliases().add(null);

		DocumentMetadata metadata = getMetadata(pushApplication, null, DocumentMetadata.NULL_DATABASE);

		// Save
		documentService.save(metadata, "{TEST PAYLOAD1}", null);
		documentService.save(metadata, "{TEST PAYLOAD2}", null);

		DocumentContent latest = documentService.findLatest(metadata, null);

		Assert.assertTrue(latest != null && latest.getContent().equals("{TEST PAYLOAD2}"));
	}

	@Test
	@Transactional
	public void testBadPhoneNumberDocument() {
		Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
		PushApplication pushApp = applicationService.findByVariantID(variant.getVariantID());

		String salias1 = "9720525679037170105113811";
		String salias2 = "9720521550826170105113811";

		List<Alias> aliases = new ArrayList<>();
		UUID pushAppId = UUID.fromString(pushApp.getPushApplicationID());
		aliases.add(new Alias(pushAppId, UUIDs.timeBased(), salias1));
		aliases.add(new Alias(pushAppId, UUIDs.timeBased(), salias2));

		aliasService.addAll(pushApp, aliases, false);

		// Reload aliases
		Alias alias1 = aliasService.find(pushApp.getPushApplicationID(), salias1);
		Alias alias2 = aliasService.find(pushApp.getPushApplicationID(), salias2);

		documentService.save(new DocumentMetadata(pushApp.getPushApplicationID(), DEFAULT_DEVICE_DATABASE, alias1),
				"{CONTENT101}", "ID301");
		documentService.save(new DocumentMetadata(pushApp.getPushApplicationID(), DEFAULT_DEVICE_DATABASE, alias2),
				"{CONTENT102}", "ID301");

		List<DocumentContent> docs = documentService.findLatest(pushApp, DEFAULT_DEVICE_DATABASE, "ID301",
				Arrays.asList(alias1, alias2));
		Assert.assertEquals(Arrays.asList(docs.get(0).getContent(), docs.get(1).getContent()),
				Arrays.asList("{CONTENT101}", "{CONTENT102}"));
	}

	@Test
	@Transactional
	public void testDocumentsWithId() {
		Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
		PushApplication pushApp = applicationService.findByVariantID(variant.getVariantID());
		String aliasstr1 = "9720525679037170105113810";
		String aliasstr2 = "9720521550826170105113810";

		List<Alias> aliases = new ArrayList<>();
		UUID pushAppId = UUID.fromString(pushApp.getPushApplicationID());
		aliases.add(new Alias(pushAppId, UUIDs.timeBased(), aliasstr1));
		aliases.add(new Alias(pushAppId, UUIDs.timeBased(), aliasstr2));

		// Create aliases using legacy code
		List<Alias> list = aliasService.addAll(pushApp, aliases, false);
		Assert.assertTrue(list.size() == 2);

		// Re-save aliases using the same numbers
		Alias alias1 = new Alias(UUID.fromString(pushApp.getPushApplicationID()), UUIDs.timeBased(), null, aliasstr1);
		Alias alias2 = new Alias(UUID.fromString(pushApp.getPushApplicationID()), UUIDs.timeBased(), null, aliasstr2);

		aliasService.create(alias1);
		aliasService.create(alias2);

		documentService.save(new DocumentMetadata(pushApp.getPushApplicationID(), DEFAULT_DEVICE_DATABASE, alias1),
				"{CONTENT1}", "ID1");
		documentService.save(new DocumentMetadata(pushApp.getPushApplicationID(), DEFAULT_DEVICE_DATABASE, alias1),
				"{CONTENT2}", "ID2");

		documentService.save(new DocumentMetadata(pushApp.getPushApplicationID(), DEFAULT_DEVICE_DATABASE, alias2),
				"{CONTENT2}", "ID1");
		documentService.save(new DocumentMetadata(pushApp.getPushApplicationID(), DEFAULT_DEVICE_DATABASE, alias2),
				"{CONTENT1000}", "ID2");

		DocumentContent doc1 = documentService
				.findLatest(getMetadata(pushApp, alias1.getOther(), DEFAULT_DEVICE_DATABASE), "ID1");
		DocumentContent doc2 = documentService
				.findLatest(getMetadata(pushApp, alias2.getOther(), DEFAULT_DEVICE_DATABASE), "ID2");

		Assert.assertEquals(doc1.getContent(), "{CONTENT1}");
		Assert.assertEquals(doc2.getContent(), "{CONTENT1000}");
	}

	@Test
	public void testDefaultDateRange() {
		UUID pushApplicationId = UUID.randomUUID();

		try {
			// Create alias specific documents
			Alias alias1 = new Alias(pushApplicationId, UUIDs.timeBased(), "supprot@aerobase.org");

			aliasService.create(alias1);

			DocumentKey key1 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
			DocumentKey key2 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));
			DocumentKey key3 = new DocumentKey(new DocumentMetadata(pushApplicationId.toString(), "STATUS", alias1));

			// Create snapshot X+1 (X defaults days) backwards.
			UUID snapshot = UUIDs.startOf(LocalDateTime.now().minusDays(configuration.getQueryDefaultPeriodInDays() + 1)
					.toInstant(ZoneOffset.UTC).toEpochMilli());
			DocumentKey key4 = new DocumentKey(new DocumentMetadata(pushApplicationId, "STATUS", alias1, snapshot));

			// Create all documents
			Thread.sleep(100);
			documentService.save(new DocumentContent(key1, "{TEST CONTENT 1}"));
			Thread.sleep(100);
			documentService.save(new DocumentContent(key2, "{TEST CONTENT 2}"));
			Thread.sleep(100);
			documentService.save(new DocumentContent(key3, "{TEST CONTENT 3}"));
			Thread.sleep(100);
			documentService.save(new DocumentContent(key4, "{TEST CONTENT 4}"));
			Thread.sleep(100);

			// Query 3 documents only
			Assert.assertTrue(
					documentService.find(new DocumentMetadata(pushApplicationId, "STATUS", alias1), new QueryOptions())
							.collect(Collectors.toList()).size() == 3);

		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}
}
