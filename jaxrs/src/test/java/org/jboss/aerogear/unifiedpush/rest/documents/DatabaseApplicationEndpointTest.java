package org.jboss.aerogear.unifiedpush.rest.documents;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.AliasEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.installations.InstallationRegistrationEndpoint;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushRestArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.datastax.driver.core.utils.UUIDs;

@RunWith(Arquillian.class)
public class DatabaseApplicationEndpointTest extends RestEndpointTest {
	@Deployment
	public static WebArchive archive() {
		return UnifiedPushRestArchive.forTestClass(DatabaseApplicationEndpointTest.class) //
				.withRest() //
				.addPackage(DatabaseApplicationEndpoint.class.getPackage()) //
				.addPackage(InstallationRegistrationEndpoint.class.getPackage()) //
				.addPackage(AliasEndpoint.class.getPackage()) //
				.as(WebArchive.class);
	}

	@BeforeClass
	public static void initResteasyClient() {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}

	@Test
	@RunAsClient
	public void storeQueySingleDocument(@ArquillianResource URL deploymentUrl) {
		Installation iosInstallation = getIosDefaultInstallation();
		Installation andInstallation = getAndroidDefaultInstallation();

		try {
			// Register first installation
			ResteasyWebTarget iosTarget = iosClient
					.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/registry/device");
			Response response = iosTarget.request()
					.post(Entity.entity(iosInstallation, MediaType.APPLICATION_JSON_TYPE));
			Installation iosNewInstallation = response.readEntity(Installation.class);

			Assert.assertTrue(response.getStatus() == 200);
			Assert.assertTrue(iosNewInstallation.isEnabled());
			response.close();

			// Register second installation
			ResteasyWebTarget andTarget = androidClient
					.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/registry/device");
			response = andTarget.request().post(Entity.entity(andInstallation, MediaType.APPLICATION_JSON_TYPE));
			Installation androidNewInstallation = response.readEntity(Installation.class);

			Assert.assertTrue(response.getStatus() == 200);
			Assert.assertTrue(androidNewInstallation.isEnabled());
			response.close();

			// Documents registration is async, lets wait a while
			Thread.sleep(500);

			String[] uuids = createAliases(deploymentUrl, iosNewInstallation, andInstallation);

			// Store document for first alias @POST /{database}/alias/{alias}
			response = DatabaseEndpointTest.saveDocument(iosClient, deploymentUrl, "RESPONSES",
					iosNewInstallation.getDeviceToken(), iosNewInstallation.getAlias(), "1", iosNewInstallation);
			response.close();
			// Store document for first alias @POST /{database}/alias/{alias}
			response = DatabaseEndpointTest.saveDocument(iosClient, deploymentUrl, "RESPONSES",
					iosNewInstallation.getDeviceToken(), iosNewInstallation.getAlias(), "2", iosNewInstallation);
			response.close();

			// Store document for second alias @POST /{database}/alias/{alias}
			response = DatabaseEndpointTest.saveDocument(androidClient, deploymentUrl, "RESPONSES",
					androidNewInstallation.getDeviceToken(), androidNewInstallation.getAlias(), "1",
					androidNewInstallation);
			response.close();
			// Store document for second alias @POST /{database}/alias/{alias}
			response = DatabaseEndpointTest.saveDocument(androidClient, deploymentUrl, "RESPONSES",
					androidNewInstallation.getDeviceToken(), androidNewInstallation.getAlias(), "2",
					androidNewInstallation);
			response.close();

			// Get documents for aliases @POST/{database}/aliases/{alias}
			response = applicationClient
					.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/database/RESPONSES/aliases/").request()
					.post(Entity.entity(uuids, MediaType.APPLICATION_JSON_TYPE));

			String count = response.getHeaderString(DatabaseEndpoint.X_HEADER_COUNT);
			Assert.assertTrue(Integer.valueOf(count) == 4);
			response.close();

			// Get documents for aliases include none existing user.
			String[] uuids2 = ArrayUtils.add(uuids, 2, UUIDs.timeBased().toString());
			response = applicationClient
					.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/database/RESPONSES/aliases/").request()
					.post(Entity.entity(uuids2, MediaType.APPLICATION_JSON_TYPE));

			count = response.getHeaderString(DatabaseEndpoint.X_HEADER_COUNT);
			DocumentList list = response.readEntity(DocumentList.class);

			Assert.assertTrue(Integer.valueOf(count) == 4);
			Assert.assertTrue(list.getIgnoredIds().size() == 1);

			response.close();
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	private String[] createAliases(@ArquillianResource URL deploymentUrl, Installation inst1, Installation inst2) {
		ResteasyWebTarget target = getAllAliasesTarget(deploymentUrl);

		List<Alias> aliases = new ArrayList<>();
		UUID id1 = UUIDs.timeBased();
		UUID id2 = UUIDs.timeBased();

		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), id1, inst1.getAlias()));
		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), id2, inst2.getAlias()));

		// Create 3 Aliases
		Response response = target.request().post(Entity.entity(aliases, MediaType.APPLICATION_JSON_TYPE));

		Assert.assertTrue(response.getStatus() == 200);
		response.close();

		return new String[] { id1.toString(), id2.toString() };
	}
}
