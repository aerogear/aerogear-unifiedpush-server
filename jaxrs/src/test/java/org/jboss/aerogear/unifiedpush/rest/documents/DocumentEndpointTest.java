package org.jboss.aerogear.unifiedpush.rest.documents;

import java.net.URL;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
@Deprecated
public class DocumentEndpointTest extends RestEndpointTest {
	private static final String RESOURCE_PREFIX = RestApplication.class.getAnnotation(ApplicationPath.class).value()
			.substring(1);

	@Deployment
	public static WebArchive archive() {
		return UnifiedPushRestArchive.forTestClass(DocumentEndpointTest.class) //
				.withRest() //
				.addPackage(DocumentEndpoint.class.getPackage()) //
				.addPackage(InstallationRegistrationEndpoint.class.getPackage()) //
				.as(WebArchive.class);
	}

	@BeforeClass
	public static void initResteasyClient() {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}

	@Test
	@RunAsClient
	// TODO - Copy and move to database api
	public void storeSimpleDocument(@ArquillianResource URL deploymentUrl) {
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

			// Store document @Path("/{alias}/{qualifier}")
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/document/installation/"
					+ newInstallation.getAlias().toUpperCase() + "/status/null");

			response = ClientAuthHelper.setDeviceToken(target.request(), newInstallation.getDeviceToken())
					.post(Entity.entity(newInstallation, MediaType.APPLICATION_JSON_TYPE));

			if (response.getStatus() != 200) {
				Assert.fail("Response status was " + response.getStatus());
			}

			// Documents snapshot is not millisecond sensitive, wait 1 second
			Thread.sleep(1000);

			response.close();

			// Update parameter and post again
			newInstallation.setOperatingSystem("TTT");
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/document/installation/"
					+ newInstallation.getAlias().toLowerCase() + "/status/null");

			response = ClientAuthHelper.setDeviceToken(target.request(), newInstallation.getDeviceToken())
					.post(Entity.entity(newInstallation, MediaType.APPLICATION_JSON_TYPE));

			if (response.getStatus() != 200) {
				Assert.fail("Response status was " + response.getStatus());
			}

			response.close();

			// get document @Path("/{publisher}/{alias}/{qualifier}/latest")
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/document/INSTALLATION/"
					+ newInstallation.getAlias().toLowerCase() + "/STATUS/null");

			response = ClientAuthHelper.setDeviceToken(target.request(), newInstallation.getDeviceToken()).get();

			Assert.assertTrue(response.getStatus() == 200);

			Installation getinst = response.readEntity(Installation.class);
			Assert.assertTrue(getinst.getAlias().equals(DEFAULT_DEVICE_ALIAS));
			Assert.assertTrue(getinst.getOperatingSystem().equals("TTT"));

		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	@RunAsClient
	// TODO - Copy and move to database api
	public void updateDocument(@ArquillianResource URL deploymentUrl) {
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

			// Store document @Path("/{alias}/{qualifier}{id}")
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/document/"
					+ newInstallation.getAlias().toLowerCase() + "/STATUS/55");

			response = ClientAuthHelper.setDeviceToken(target.request(), newInstallation.getDeviceToken())
					.put(Entity.entity(newInstallation, MediaType.APPLICATION_JSON_TYPE));

			if (response.getStatus() != 200) {
				Assert.fail("Response status was " + response.getStatus());
			}

			response.close();

			// Update parameter and put again
			newInstallation.setOperatingSystem("XXX");
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/document/"
					+ newInstallation.getAlias() + "/STATUS/55");

			response = ClientAuthHelper.setDeviceToken(target.request(), newInstallation.getDeviceToken())
					.put(Entity.entity(newInstallation, MediaType.APPLICATION_JSON_TYPE));

			response.close();

			// get document @Path("/{alias}/{qualifier}/{id}/latest")
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/document/INSTALLATION/"
					+ newInstallation.getAlias() + "/STATUS/55");

			response = ClientAuthHelper.setDeviceToken(target.request(), newInstallation.getDeviceToken()).get();

			Assert.assertTrue(response.getStatus() == 200);

			Installation getinst = response.readEntity(Installation.class);
			Assert.assertTrue(getinst.getOperatingSystem().equals("XXX"));

		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}
}
