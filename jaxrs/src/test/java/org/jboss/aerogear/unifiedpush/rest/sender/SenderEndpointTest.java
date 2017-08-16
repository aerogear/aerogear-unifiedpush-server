package org.jboss.aerogear.unifiedpush.rest.sender;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.document.DocumentDeployMessage;
import org.jboss.aerogear.unifiedpush.document.MessagePayload;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.aerogear.unifiedpush.rest.util.Authenticator;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { WebConfigTest.class })
public class SenderEndpointTest extends RestEndpointTest {
	@Test
	public void storeSimpleDocument() {
		ResteasyClient client = new ResteasyClientBuilder()
				.register(new Authenticator(DEFAULT_APP_ID, DEFAULT_APP_PASS)).build();

		try {
			// First prepare push message with large payload
			DocumentDeployMessage message = new DocumentDeployMessage();
			message.addPayload(new MessagePayload(null, "{TEST}"));

			ResteasyWebTarget target = client.target(getRestFullPath() + "/sender/payload");
			Response response = target.request().post(Entity.entity(message, MediaType.APPLICATION_JSON_TYPE));

			Assert.assertTrue(response.getStatus() == 200);
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

}
