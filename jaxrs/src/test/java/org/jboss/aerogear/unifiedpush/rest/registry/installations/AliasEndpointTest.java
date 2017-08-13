package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.Alias;
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

		Alias original = new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Supprot@AeroBase.org");
		// Create Alias
		Response response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS)
				.post(Entity.entity(original, MediaType.APPLICATION_JSON_TYPE));

		Assert.assertTrue(response.getStatus() == 200);
		response.close();

		// Query for previously created alias by alias lower(name)
		target = client.target(
				deploymentUrl.toString() + RESOURCE_PREFIX + "/alias/name/" + original.getEmail().toLowerCase());

		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
		Assert.assertTrue(response.getStatus() == 200);
		Alias alias = response.readEntity(Alias.class);
		Assert.assertTrue(alias != null & alias.getEmail().equals(original.getEmail()));
		response.close();

		// Query for previously created alias by alias id
		target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/alias/" + original.getId());

		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
		Assert.assertTrue(response.getStatus() == 200);
		alias = response.readEntity(Alias.class);
		Assert.assertTrue(alias != null & alias.getEmail().equals(original.getEmail()));
		response.close();
	}

	@Test
	@RunAsClient
	public void registerAliases(@ArquillianResource URL deploymentUrl) {
		ResteasyWebTarget target = getAllAliasesTarget(deploymentUrl);

		List<Alias> aliases = new ArrayList<>();
		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Supprot@AeroBase.org"));
		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Test@AeroBase.org"));
		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Help@AeroBase.org"));

		// Create 3 Aliases
		Response response = target.request().post(Entity.entity(aliases, MediaType.APPLICATION_JSON_TYPE));

		Assert.assertTrue(response.getStatus() == 200);
		response.close();

		// Re-Create 3 Aliases
		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS)
				.post(Entity.entity(aliases, MediaType.APPLICATION_JSON_TYPE));
		Assert.assertTrue(response.getStatus() == 200);
		response.close();

		// Re-Create 2 first Aliases
		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS)
				.post(Entity.entity(aliases.subList(0, 1), MediaType.APPLICATION_JSON_TYPE));
		Assert.assertTrue(response.getStatus() == 200);
		response.close();

		// Query for previously created aliases
		for (Alias alias : aliases) {
			target = getAliasByNameTarget(deploymentUrl, alias.getEmail());

			response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
			Assert.assertTrue(response.getStatus() == 200);
			Alias aliasObj = response.readEntity(Alias.class);
			Assert.assertTrue(aliasObj != null & aliasObj.getEmail().equals(alias.getEmail()));
			response.close();
		}
	}

	@Test
	@RunAsClient
	public void delete(@ArquillianResource URL deploymentUrl) {
		ResteasyClient client = new ResteasyClientBuilder().build();

		ResteasyWebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/alias");

		Alias original = new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Supprot888@AeroBase.org");
		// Create Alias
		Response response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS)
				.post(Entity.entity(original, MediaType.APPLICATION_JSON_TYPE));

		Assert.assertTrue(response.getStatus() == 200);
		response.close();

		// Query for previously created alias by alias lower(name)
		target = client.target(
				deploymentUrl.toString() + RESOURCE_PREFIX + "/alias/name/" + original.getEmail().toLowerCase());

		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
		Assert.assertTrue(response.getStatus() == 200);
		Alias alias = response.readEntity(Alias.class);
		Assert.assertTrue(alias != null & alias.getEmail().equals(original.getEmail()));
		Assert.assertTrue(alias != null & alias.getId().equals(original.getId()));
		response.close();

		// Delete alias
		target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/alias/" + alias.getId());
		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).delete();
		Assert.assertTrue(response.getStatus() == 200);
		response.close();

		// Query for previously deleted alias by alias id
		target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/alias/" + original.getId());

		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
		Assert.assertTrue(response.getStatus() == 200);
		Assert.assertTrue(!response.hasEntity());
		response.close();
	}
}