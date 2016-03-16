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
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushServiceArchive;
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
public class DocumentEndpointTest extends RestEndpointTest {
	private static final String RESOURCE_PREFIX = RestApplication.class.getAnnotation(ApplicationPath.class).value()
			.substring(1);

	@Deployment
	public static WebArchive archive() {
		return UnifiedPushServiceArchive.forTestClass(DocumentEndpointTest.class)
				.addMavenDependencies("org.jboss.aerogear.unifiedpush:unifiedpush-service")
				.addAsLibrary("org.jboss.aerogear.unifiedpush:unifiedpush-model-jpa",
						new String[] { "META-INF/persistence.xml", "test-data.sql" },
						new String[] { "META-INF/test-persistence.xml", "META-INF/test-data.sql" })
				.addPackage(RestApplication.class.getPackage())
				.addPackage(InstallationRegistrationEndpoint.class.getPackage())
				.addPackage(DocumentEndpoint.class.getPackage())
				.addClasses(RestEndpointTest.class, RestApplication.class, HttpBasicHelper.class, Authenticator.class,
						ClientAuthHelper.class)
				.addAsWebInfResource("META-INF/test-ds.xml", "test-ds.xml")
				.addAsResource("test.properties", "default.properties").as(WebArchive.class);
	}

	@BeforeClass
	public static void initResteasyClient() {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}

	@Test
	@RunAsClient
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

			// Store document @Path("/{publisher}/{alias}/{qualifier}{id}")
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/document/INSTALLATION/"
					+ newInstallation.getAlias() + "/STATUS");

			// Documents registration is async, lets wait a while
			Thread.sleep(500);

			response = target.request()
					.header(ClientAuthHelper.DEVICE_TOKEN_HEADER,
							HttpBasicHelper.encodeBase64(newInstallation.getDeviceToken()))
					.post(Entity.entity(newInstallation, MediaType.APPLICATION_JSON_TYPE));

			if (response.getStatus() != 200) {
				Assert.fail("Response status was " + response.getStatus());
			}

			response.close();

			// get document @Path("/{publisher}/{alias}/{qualifier}/latest")
			target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/document/INSTALLATION/"
					+ newInstallation.getAlias() + "/STATUS/latest");

			response = target.request().header(ClientAuthHelper.DEVICE_TOKEN_HEADER,
					HttpBasicHelper.encodeBase64(newInstallation.getDeviceToken())).get();

			Assert.assertTrue(response.getStatus() == 200);

			Installation getinst = response.readEntity(Installation.class);
			Assert.assertTrue(getinst.isEnabled());

		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

}
