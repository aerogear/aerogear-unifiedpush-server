package org.jboss.aerogear.unifiedpush.rest.documents;

import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.rest.RestApplication;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.registry.installations.InstallationRegistrationEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.Authenticator;
import org.jboss.aerogear.unifiedpush.rest.util.ClientAuthHelper;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushRestArchive;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
	public void storeQueySingleDocument(@ArquillianResource URL deploymentUrl) {
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
			response = saveDocument(client, deploymentUrl, "STATUS", newInstallation.getDeviceToken(),
					newInstallation.getAlias(), "1", newInstallation);

			// Validate new snapshot id exists.
			String snapshotId1 = response.getHeaderString(DatabaseEndpoint.X_HEADER_SNAPSHOT_ID);
			assertTrue(StringUtils.isNoneEmpty(snapshotId1));

			response.close();

			// Update parameter and PUT
			newInstallation.setOperatingSystem("XXX");
			// Store document for alias @PUT /{database}/alias/{alias}/{UUID}
			response = saveDocument(client, deploymentUrl, "STATUS", newInstallation.getDeviceToken(),
					newInstallation.getAlias(), "1", snapshotId1, newInstallation);

			// Validate new snapshot id exists.
			String snapshotId2 = response.getHeaderString(DatabaseEndpoint.X_HEADER_SNAPSHOT_ID);
			assertTrue(StringUtils.isNoneEmpty(snapshotId2));
			assertTrue(snapshotId1.equals(snapshotId2));

			response.close();

			// Store additional document
			response = saveDocument(client, deploymentUrl, "STATUS", newInstallation.getDeviceToken(),
					newInstallation.getAlias(), "2", newInstallation);

			response.close();

			// get documents @GET /{database}/alias/{alias}
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/database/STATUS/alias/"
					+ newInstallation.getAlias().toLowerCase());

			// Additional Accept header to also test MediaType.APPLICATION_JSON
			// content
			response = ClientAuthHelper.setDeviceToken(target.request(), newInstallation.getDeviceToken())
					.header("Accept", MediaType.APPLICATION_JSON).get();

			Assert.assertTrue(response.getStatus() == 200);
			String count = response.getHeaderString(DatabaseEndpoint.X_HEADER_COUNT);
			Assert.assertTrue(Integer.valueOf(count) == 2);

			response.close();

			// get documents @HEAD /{database}/alias/{alias}
			response = ClientAuthHelper.setDeviceToken(target.request(), newInstallation.getDeviceToken()).head();

			Assert.assertTrue(response.getStatus() == 204);
			count = response.getHeaderString(DatabaseEndpoint.X_HEADER_COUNT);
			Assert.assertTrue(Integer.valueOf(count) == 2);
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	private Response saveDocument(ResteasyClient client, URL deploymentUrl, String databse, String deviceToken,
			String alias, String id, Object... jsonEntity) {
		return saveDocument(client, deploymentUrl, databse, deviceToken, alias, id, null, jsonEntity);

	}

	// Store document for alias @POST /{database}/alias/{alias}
	private Response saveDocument(ResteasyClient client, URL deploymentUrl, String databse, String deviceToken,
			String alias, String id, String snapshot, Object... jsonEntity) {

		StringBuilder sBuilder = new StringBuilder(deploymentUrl.toString() + RESOURCE_PREFIX + "/database/" + databse);
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

		Builder builder = ClientAuthHelper.setDeviceToken(target.request(), deviceToken);

		Response response;

		if (StringUtils.isNoneEmpty(snapshot)) {
			// Update by snapshot
			response = builder.put(Entity.entity(getEntity(jsonEntity), getContentType(jsonEntity)));
		} else {
			// Create new document
			response = builder.post(Entity.entity(getEntity(jsonEntity), getContentType(jsonEntity)));
		}

		if (response.getStatus() != 204) {
			Assert.fail("Response status was " + response.getStatus());
		}

		return response;
	}

	private Object getEntity(Object... jsonEntity) {
		if (jsonEntity.length == 1) {
			return jsonEntity[0];
		} else {
			final MultipartOutput output = new MultipartOutput();
			List<Object> list = Arrays.stream(jsonEntity).collect(Collectors.toList());

			list.forEach(entity -> {
				output.addPart(entity, MediaType.APPLICATION_JSON_TYPE);
			});

			return output;
		}
	}

	private MediaType getContentType(Object... jsonEntity) {
		if (jsonEntity.length == 1) {
			return MediaType.APPLICATION_JSON_TYPE;
		} else {
			return MediaType.MULTIPART_FORM_DATA_TYPE;
		}
	}

	@Test
	@RunAsClient
	public void storeQueyMultiPartDocuments(@ArquillianResource URL deploymentUrl) {
		Installation iosInstallation = getDefaultInstallation();

		ResteasyClient client = new ResteasyClientBuilder()
				.register(new Authenticator(DEFAULT_VARIENT_ID, DEFAULT_VARIENT_PASS)).build();

		// First register installation
		ResteasyWebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/registry/device");
		Response response = target.request().post(Entity.entity(iosInstallation, MediaType.APPLICATION_JSON_TYPE));
		Installation newInstallation = response.readEntity(Installation.class);

		Assert.assertTrue(response.getStatus() == 200);
		Assert.assertTrue(newInstallation.isEnabled());

		try {
			// Documents registration is async, lets wait a while
			Thread.sleep(500);

			// Store document for alias @POST /databse/{database}
			response = saveDocument(client, deploymentUrl, "DEVICES", newInstallation.getDeviceToken(), null, null,
					new DocumentInstallationWrapper(newInstallation), new DocumentInstallationWrapper(newInstallation),
					new DocumentInstallationWrapper(newInstallation));

			response.close();

			// Store document for alias @POST /databse/{database}/alias/{alias}
			response = saveDocument(client, deploymentUrl, "DEVICES", newInstallation.getDeviceToken(),
					newInstallation.getAlias(), null, new DocumentInstallationWrapper(newInstallation),
					new DocumentInstallationWrapper(newInstallation), new DocumentInstallationWrapper(newInstallation));

		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}
}
