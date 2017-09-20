package org.jboss.aerogear.unifiedpush.rest.documents;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ArrayUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.transaction.annotation.Transactional;

import com.datastax.driver.core.utils.UUIDs;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { WebConfigTest.class })
public class DatabaseApplicationEndpointTest extends RestEndpointTest {
	@Test
	@Transactional
	public void storeQueySingleDocument() {
		Installation iosInstallation = getIosDefaultInstallation();
		Installation andInstallation = getAndroidDefaultInstallation();

		try {
			// Register first installation
			ResteasyWebTarget iosTarget = iosClient.target(getRestFullPath() + "/registry/device");
			Response response = iosTarget.request()
					.post(Entity.entity(iosInstallation, MediaType.APPLICATION_JSON_TYPE));
			Installation iosNewInstallation = response.readEntity(Installation.class);

			assertEquals(200, response.getStatus());
			Assert.assertTrue(iosNewInstallation.isEnabled());
			response.close();

			// Register second installation
			ResteasyWebTarget andTarget = androidClient
					.target(getRestFullPath() + "/registry/device");
			response = andTarget.request().post(Entity.entity(andInstallation, MediaType.APPLICATION_JSON_TYPE));
			Installation androidNewInstallation = response.readEntity(Installation.class);

			assertEquals(200, response.getStatus());
			Assert.assertTrue(androidNewInstallation.isEnabled());
			response.close();

			// Documents registration is async, lets wait a while
			Thread.sleep(500);

			String[] uuids = createAliases(iosNewInstallation, andInstallation);

			// Store document for first alias @POST /{database}/alias/{alias}
			response = DatabaseEndpointTest.saveDocument(iosClient, getRestFullPath(), "RESPONSES",
					iosNewInstallation.getDeviceToken(), iosNewInstallation.getAlias(), "1", iosNewInstallation);
			response.close();
			// Store document for first alias @POST /{database}/alias/{alias}
			response = DatabaseEndpointTest.saveDocument(iosClient, getRestFullPath(), "RESPONSES",
					iosNewInstallation.getDeviceToken(), iosNewInstallation.getAlias(), "2", iosNewInstallation);
			response.close();

			// Store document for second alias @POST /{database}/alias/{alias}
			response = DatabaseEndpointTest.saveDocument(androidClient, getRestFullPath(), "RESPONSES",
					androidNewInstallation.getDeviceToken(), androidNewInstallation.getAlias(), "1",
					androidNewInstallation);
			response.close();
			// Store document for second alias @POST /{database}/alias/{alias}
			response = DatabaseEndpointTest.saveDocument(androidClient, getRestFullPath(), "RESPONSES",
					androidNewInstallation.getDeviceToken(), androidNewInstallation.getAlias(), "2",
					androidNewInstallation);
			response.close();

			// Get documents for aliases @POST/{database}/aliases/{alias}
			response = applicationClient.target(getRestFullPath() + "/database/RESPONSES/aliases/").request()
					.post(Entity.entity(uuids, MediaType.APPLICATION_JSON_TYPE));

			String count = response.getHeaderString(DatabaseEndpoint.X_HEADER_COUNT);
			Assert.assertTrue(Integer.valueOf(count) == 4);
			response.close();

			// Get documents for aliases include none existing user.
			String[] uuids2 = ArrayUtils.add(uuids, 2, UUIDs.timeBased().toString());
			response = applicationClient.target(getRestFullPath() + "/database/RESPONSES/aliases/").request()
					.post(Entity.entity(uuids2, MediaType.APPLICATION_JSON_TYPE));

			count = response.getHeaderString(DatabaseEndpoint.X_HEADER_COUNT);
			DocumentList list = response.readEntity(DocumentList.class);

			Assert.assertTrue(Integer.valueOf(count) == 4);
			Assert.assertTrue(list.getIgnoredIds().size() == 1);

			response.close();
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	private String[] createAliases(Installation inst1, Installation inst2) {
		ResteasyWebTarget target = getAllAliasesTarget(getRestFullPath());

		List<Alias> aliases = new ArrayList<>();
		UUID id1 = UUIDs.timeBased();
		UUID id2 = UUIDs.timeBased();

		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), id1, inst1.getAlias()));
		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), id2, inst2.getAlias()));

		// Create 3 Aliases
		Response response = target.request().post(Entity.entity(aliases, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(200, response.getStatus());
		response.close();

		return new String[] { id1.toString(), id2.toString() };
	}
}
