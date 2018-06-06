package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.InstallationVerificationAttempt;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.aerogear.unifiedpush.rest.util.Authenticator;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.VerificationService;
import org.jboss.aerogear.unifiedpush.service.VerificationService.VerificationResult;
import org.jboss.aerogear.unifiedpush.system.ConfigurationEnvironment;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.datastax.driver.core.utils.UUIDs;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { WebConfigTest.class })
public class InstallationRegistrationEndpointTest extends RestEndpointTest {
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

	@Test
	public void unAuthorizedDeviceTest() {
		ResteasyClient client = new ResteasyClientBuilder().build();

		ResteasyWebTarget target = client.target(getRestFullPath() + "/registry/device");
		Response response = target.request().post(Entity.entity(new Installation(), MediaType.APPLICATION_JSON_TYPE));

		assertEquals(401, response.getStatus());
	}

	@Test
	@Transactional
	public void registerDeviceTest() {
		// Prepare installation
		Installation iosInstallation = getIosDefaultInstallation();

		ResteasyClient client = new ResteasyClientBuilder()
				.register(new Authenticator(DEFAULT_IOS_VARIENT_ID, DEFAULT_IOS_VARIENT_PASS)).build();

		try {
			ResteasyWebTarget target = client.target(getRestFullPath() + "/registry/device");
			Response response = target.request().post(Entity.entity(iosInstallation, MediaType.APPLICATION_JSON_TYPE));
			Installation newInstallation = response.readEntity(Installation.class);

			assertEquals(200, response.getStatus());
			Assert.assertTrue(newInstallation.isEnabled());
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	@Transactional
	public void enableDeviceTest() {
		System.setProperty(ConfigurationEnvironment.PROP_ENABLE_VERIFICATION, "true");

		// Prepare installation
		Installation iosInstallation = getIosDefaultInstallation();
		// Also check case sensitive aliases
		iosInstallation.setAlias("SupporT@test.com");
		iosInstallation.setDeviceToken(DEFAULT_IOS_DEVICE_TOKEN.replace("40", "50"));

		try {
			Variant variant = genericVariantService.findByVariantID(DEFAULT_IOS_VARIENT_ID);
			assertEquals(variant.getVariantID(), DEFAULT_IOS_VARIENT_ID);

			installationService.addInstallation(variant, iosInstallation);

			Installation inst = installationService.findById(iosInstallation.getId());
			assertTrue("Installation is null", inst != null);
			assertEquals(inst.isEnabled(), true);

			// Register alias
			PushApplication app = applicationService.findByVariantID(variant.getVariantID());

			List<Alias> aliases = new ArrayList<>();
			aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Support@Test.com"));
			aliasService.addAll(app, aliases, false);

			// ReEnable device
			String code = verificationService.initiateDeviceVerification(inst, variant);
			VerificationResult results = verificationService.verifyDevice(inst, variant,
					new InstallationVerificationAttempt(code, inst.getDeviceToken()));
			assertTrue("Result is null", results != null);
			assertEquals(results, VerificationResult.SUCCESS);

			Variant var = installationService.associateInstallation(inst, variant);
			assertTrue("Variant is null", var != null);
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		} finally {
			System.clearProperty(ConfigurationEnvironment.PROP_ENABLE_VERIFICATION);
		}
	}
}