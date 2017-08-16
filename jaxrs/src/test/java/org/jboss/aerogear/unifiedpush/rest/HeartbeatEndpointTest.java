package org.jboss.aerogear.unifiedpush.rest;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { WebConfigTest.class })
public class HeartbeatEndpointTest extends RestEndpointTest {

	@Test
	public void testHeartbeatEndpoint() {
		String uuid = testTemplate.getForObject(getRestFullPath() + "/heartbeat", String.class);

		assertTrue(uuid != null && UUID.fromString(uuid.replaceAll("\"", "")).toString() != null);
	}
}
