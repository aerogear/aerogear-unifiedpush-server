package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { WebConfigTest.class })
public class PushApplicationEndpointTest extends RestEndpointTest {
	@Test
	public void testDeveloperAccess() {

		ResteasyClient client = new ResteasyClientBuilder().build();

		ResteasyWebTarget target = client.target(getRestFullPath() + "/applications");

		PushApplication pushApplication = new PushApplication();
		pushApplication.setName("TEST APP 1");

		// Create admin Application
		Response response = HttpBasicHelper.basic(target.request(), DEFAULT_USER, "password")
				.post(Entity.entity(pushApplication, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(201, response.getStatus());
		response.close();

		pushApplication = new PushApplication();
		pushApplication.setName("TEST APP 2");

		// Create developer Application
		response = HttpBasicHelper.basic(target.request(), "developer", "password")
				.post(Entity.entity(pushApplication, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(201, response.getStatus());
		response.close();
	}

	@Test
	public void testUniqueApplicationName() {

		ResteasyClient client = new ResteasyClientBuilder().build();

		ResteasyWebTarget target = client.target(getRestFullPath() + "/applications");

		PushApplication pushApplication = new PushApplication();
		pushApplication.setName("TEST APP 3");

		// Create admin Application
		Response response = HttpBasicHelper.basic(target.request(), DEFAULT_USER, "password")
				.post(Entity.entity(pushApplication, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(201, response.getStatus());
		response.close();

		// Create the same Application
		response = HttpBasicHelper.basic(target.request(), DEFAULT_USER, "password")
				.post(Entity.entity(pushApplication, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(400, response.getStatus());
		@SuppressWarnings("unchecked")
		Map<String, String> message = response.readEntity(HashMap.class);

		String code = pushApplication.getClass().getName().toLowerCase() + ".unique.name";
		assertEquals(message.keySet().contains(code), true);
		assertEquals(message.get(code).contains("'" + pushApplication.getName() + "'"), true);

		response.close();
	}
}
