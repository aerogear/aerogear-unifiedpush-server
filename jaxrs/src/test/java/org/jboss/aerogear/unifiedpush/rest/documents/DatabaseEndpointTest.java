package org.jboss.aerogear.unifiedpush.rest.documents;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.rest.RestApplication;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.registry.installations.InstallationRegistrationEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.Authenticator;
import org.jboss.aerogear.unifiedpush.rest.util.ClientAuthHelper;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushRestArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import javax.ws.rs.client.Invocation.Builder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DatabaseEndpointTest extends RestEndpointTest {
	private static final String RESOURCE_PREFIX = RestApplication.class.getAnnotation(ApplicationPath.class).value()
			.substring(1);

	@Deployment
	public static WebArchive archive() {
		return UnifiedPushRestArchive.forTestClass(DatabaseEndpointTest.class) //
				.withRest() //
				.addPackage(DatabaseEndpoint.class.getPackage()) //
				.addPackage(InstallationRegistrationEndpoint.class.getPackage()) //
				.as(WebArchive.class);
	}

	@BeforeClass
	public static void initResteasyClient() {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}

	@Test
	@RunAsClient
	public void getDocuments(@ArquillianResource URL deploymentUrl) {
		Installation iosInstallation = getDefaultInstallation();

		ResteasyClient client = new ResteasyClientBuilder()
				.register(new Authenticator(DEFAULT_VARIENT_ID, DEFAULT_VARIENT_PASS)).build();

		// First register installation
		try {
			ResteasyWebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/registry/device");
			Response response = target.request().post(Entity.entity(iosInstallation, MediaType.APPLICATION_JSON_TYPE));
			Installation newInstallation = response.readEntity(Installation.class);

			Assert.assertTrue(response.getStatus() == 200);
			Assert.assertTrue(newInstallation.isEnabled());

			// Documents registration is async, lets wait a while
			Thread.sleep(500);

			// Store document for alias @POST /{database}/alias/{alias}
			response = saveDocument(client, deploymentUrl, newInstallation.getDeviceToken(), newInstallation.getAlias(),
					"1", newInstallation);

			// Validate new snapshot id exists.
			String snapshotId1 = response.getHeaderString(DatabaseEndpoint.X_HEADER_SNAPSHOT_ID);
			assertTrue(StringUtils.isNoneEmpty(snapshotId1));

			response.close();

			// Update parameter and PUT
			newInstallation.setOperatingSystem("XXX");
			// Store document for alias @PUT /{database}/alias/{alias}/{UUID}
			response = saveDocument(client, deploymentUrl, newInstallation.getDeviceToken(), newInstallation.getAlias(),
					"1", newInstallation, snapshotId1);

			// Validate new snapshot id exists.
			String snapshotId2 = response.getHeaderString(DatabaseEndpoint.X_HEADER_SNAPSHOT_ID);
			assertTrue(StringUtils.isNoneEmpty(snapshotId2));
			assertTrue(snapshotId1.equals(snapshotId2));

			response.close();

			// Store additional document
			response = saveDocument(client, deploymentUrl, newInstallation.getDeviceToken(), newInstallation.getAlias(),
					"2", newInstallation);

			response.close();

			// get documents @GET /{database}/alias/{alias}
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/database/STATUS/alias/"
					+ newInstallation.getAlias().toLowerCase());

			response = target.request().header(ClientAuthHelper.DEVICE_TOKEN_HEADER,
					HttpBasicHelper.encodeBase64(newInstallation.getDeviceToken())).get();

			Assert.assertTrue(response.getStatus() == 200);

			String count = response.getHeaderString(DatabaseEndpoint.X_HEADER_COUNT);
			Assert.assertTrue(StringUtils.isNoneEmpty(count));
			Assert.assertTrue(Integer.valueOf(count) == 2);
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	private Response saveDocument(ResteasyClient client, URL deploymentUrl, String deviceToken, String alias, String id,
			Object jsonEntity) {
		return saveDocument(client, deploymentUrl, deviceToken, alias, id, jsonEntity, null);

	}

	// Store document for alias @POST /{database}/alias/{alias}
	private Response saveDocument(ResteasyClient client, URL deploymentUrl, String deviceToken, String alias, String id,
			Object jsonEntity, String snapshot) {

		StringBuilder sBuilder = new StringBuilder(deploymentUrl.toString() + RESOURCE_PREFIX + "/database/STATUS");
		if (StringUtils.isNoneEmpty(alias)) {
			sBuilder.append("/alias/").append(alias.toLowerCase());
		}

		if (StringUtils.isNoneEmpty(snapshot)) {
			sBuilder.append("/" + snapshot);
		}

		if (StringUtils.isNoneEmpty(id)) {
			sBuilder.append("?id=" + id);
		}

		ResteasyWebTarget target = client.target(sBuilder.toString());

		Builder builder = target.request().header(ClientAuthHelper.DEVICE_TOKEN_HEADER,
				HttpBasicHelper.encodeBase64(deviceToken));

		Response response;
		if (StringUtils.isNoneEmpty(snapshot)) {
			// Update by snapshot
			response = builder.put(Entity.entity(jsonEntity, MediaType.APPLICATION_JSON_TYPE));
		} else {
			// Create new document
			response = builder.post(Entity.entity(jsonEntity, MediaType.APPLICATION_JSON_TYPE));
		}

		if (response.getStatus() != 200) {
			Assert.fail("Response status was " + response.getStatus());
		}

		return response;
	}
}
