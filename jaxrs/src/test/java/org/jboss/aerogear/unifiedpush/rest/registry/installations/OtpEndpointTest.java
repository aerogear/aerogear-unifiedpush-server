package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
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
public class OtpEndpointTest extends RestEndpointTest {

	@Test
	@Transactional
	public void registerDeviceTest() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		ResteasyWebTarget target = client.target(getRestFullPath() + "/alias");

		Alias original = new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "supprot@aerogear.org");
		// Create Alias
		Response response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS)
				.post(Entity.entity(original, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(200, response.getStatus());
		response.close();

		try {
			// Send OTP
			target = client.target(getRestFullPath() + "/otp/" + original.getEmail().toLowerCase() + "?reset=true");
			response = target.request().get();
			Assert.assertTrue(response.getStatus() == 204);
			response.close();
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}


}