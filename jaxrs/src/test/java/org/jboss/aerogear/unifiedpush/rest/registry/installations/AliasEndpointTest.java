package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import java.net.URL;
import java.util.UUID;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.rest.RestApplication;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.AliasEndpoint;
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
import org.junit.Test;
import org.junit.runner.RunWith;

import com.datastax.driver.core.utils.UUIDs;

@RunWith(Arquillian.class)
public class AliasEndpointTest extends RestEndpointTest {
	private static final String RESOURCE_PREFIX = RestApplication.class.getAnnotation(ApplicationPath.class).value()
			.substring(1);

	@Deployment
	public static WebArchive archive() {
		return UnifiedPushRestArchive.forTestClass(AliasEndpointTest.class) //
				.withRest() //
				.addPackage(AliasEndpoint.class.getPackage()) //
				.as(WebArchive.class);
	}

	@BeforeClass
	public static void initResteasyClient() {
		RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
	}

	@Test
	@RunAsClient
	public void registerAlias(@ArquillianResource URL deploymentUrl) {
		ResteasyClient client = new ResteasyClientBuilder().build();

		ResteasyWebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/alias");

		// Create Alias
		Response response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS)
				.post(Entity.entity( //
						new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Supprot@AeroBase.org"), //
						MediaType.APPLICATION_JSON_TYPE));

		Assert.assertTrue(response.getStatus() == 200);
		response.close();

		// Query for previously created alias
		target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/alias/" + "supprot@aerobase.org");

		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
		Assert.assertTrue(response.getStatus() == 200);
		Alias alias = response.readEntity(Alias.class);

		Assert.assertTrue(alias != null & alias.getEmail().equals("supprot@aerobase.org"));
	}

}