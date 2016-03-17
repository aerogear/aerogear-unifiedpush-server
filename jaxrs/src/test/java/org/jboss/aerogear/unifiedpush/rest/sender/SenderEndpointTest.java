package org.jboss.aerogear.unifiedpush.rest.sender;

import java.net.URL;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.document.DocumentDeployMessage;
import org.jboss.aerogear.unifiedpush.document.MessagePayload;
import org.jboss.aerogear.unifiedpush.rest.RestApplication;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@Ignore
@RunWith(Arquillian.class)
public class SenderEndpointTest extends RestEndpointTest {
	private static final String RESOURCE_PREFIX = RestApplication.class.getAnnotation(ApplicationPath.class).value()
			.substring(1);

	@Deployment
	public static WebArchive archive() {
		return UnifiedPushRestArchive.forTestClass(SenderEndpointTest.class)
				.addMavenDependencies("org.jboss.aerogear.unifiedpush:unifiedpush-push-sender")
				.addMavenDependencies("com.fasterxml.jackson.module:jackson-module-jaxb-annotations")
				.addAsLibrary("org.jboss.aerogear.unifiedpush:unifiedpush-model-jpa",
						new String[] { "META-INF/persistence.xml", "test-data.sql" },
						new String[] { "META-INF/test-persistence.xml", "META-INF/test-data.sql" })
				.addPackage(RestApplication.class.getPackage())
				.addPackage(PushNotificationSenderEndpoint.class.getPackage())
				.addClasses(RestEndpointTest.class, RestApplication.class, HttpBasicHelper.class, Authenticator.class,
						ClientAuthHelper.class)
				.addAsWebInfResource("jboss-ejb3-message-holder-with-tokens.xml", "jboss-ejb3.xml")
				.addAsWebInfResource("hornetq-jms.xml").addAsWebInfResource("META-INF/test-ds.xml", "test-ds.xml")
				.addAsResource("test.properties", "default.properties").as(WebArchive.class);
	}

	@BeforeClass
	public static void initResteasyClient() {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}

	@Test
	@RunAsClient
	public void storeSimpleDocument(@ArquillianResource URL deploymentUrl) {
		ResteasyClient client = new ResteasyClientBuilder()
				.register(new Authenticator(DEFAULT_VARIENT_ID, DEFAULT_VARIENT_PASS)).build();

		try {
			// First prepare push message with large payload
			DocumentDeployMessage message = new DocumentDeployMessage();
			message.setGlobalPayload(new MessagePayload(null, "{TEST}"));

			ResteasyWebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/sender/payload");
			Response response = target.request().post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

			Assert.assertTrue(response.getStatus() == 200);
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

}
