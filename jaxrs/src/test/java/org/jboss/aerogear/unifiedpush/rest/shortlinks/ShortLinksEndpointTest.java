package org.jboss.aerogear.unifiedpush.rest.shortlinks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.aerogear.unifiedpush.service.AliasService;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
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
public class ShortLinksEndpointTest extends RestEndpointTest {
	@Inject
	private GenericVariantService genericVariantService;
	@Inject
	private ClientInstallationService installationService;
	@Inject
	private PushApplicationService applicationService;
	@Inject
	private AliasService aliasService;
	
	@Test
	@Transactional
	public void storeQueySingleDocument() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		String alias = "Support@Test.com";
		registerAlias(alias);
		
		// First register installation
		try {
			ResteasyWebTarget target = client.target(getRestFullPath() + "/shortlinks/type/code/username/" + alias);
			Response response = target.request().put(Entity.entity("https://www.google.com", MediaType.TEXT_PLAIN));
			String link = response.readEntity(String.class);

			Assert.assertTrue(response.getStatus() == 200);
			Assert.assertTrue(StringUtils.isNoneEmpty(link));
			
			response.close();
			
			target = client.target(getRestFullPath() + "/shortlinks/" + ShortLinksEndpoint.getCodeFromLink(link));
			response = target.request().get();
			Assert.assertTrue(response.getStatus() == 307);
			
			response.close();
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}

	
	private void registerAlias(String alias) {
		// Prepare installation
		Installation iosInstallation = getIosDefaultInstallation();
		// Also check case sensitive aliases
		iosInstallation.setAlias("SupporT@test.com");
		iosInstallation.setDeviceToken(DEFAULT_IOS_DEVICE_TOKEN.replace("40", "50"));

		try {
			Variant variant = genericVariantService.findByVariantID(DEFAULT_IOS_VARIENT_ID);
			assertEquals(variant.getVariantID(), DEFAULT_IOS_VARIENT_ID);

			installationService.addInstallation(variant, iosInstallation);

			Installation inst = installationService.findById(iosInstallation.getId());
			assertTrue("Installation is null", inst != null);
			assertEquals(inst.isEnabled(), true);

			// Register alias
			PushApplication app = applicationService.findByVariantID(variant.getVariantID());

			List<Alias> aliases = new ArrayList<>();
			aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), alias));
			aliasService.addAll(app, aliases, false);
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
	}
}
