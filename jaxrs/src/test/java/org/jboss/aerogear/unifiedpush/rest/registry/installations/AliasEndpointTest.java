package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.jboss.aerogear.unifiedpush.api.Alias;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.cassandra.dao.AliasDao;
import org.jboss.aerogear.unifiedpush.rest.RestEndpointTest;
import org.jboss.aerogear.unifiedpush.rest.WebConfigTest;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.service.annotations.LoggedInUser;
import org.jboss.aerogear.unifiedpush.service.impl.AliasServiceImpl;
import org.jboss.aerogear.unifiedpush.service.impl.spring.IKeycloakService;
import org.jboss.aerogear.unifiedpush.service.impl.spring.OAuth2Configuration;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.datastax.driver.core.utils.UUIDs;
import com.google.gson.Gson;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = { WebConfigTest.class })
public class AliasEndpointTest extends RestEndpointTest {

	public static final String LOCALHOST = "localhost";
	public static final UUID APP_ID = UUIDs.timeBased();
	public static final String OTHER = "other-";

	private static final String realmName = "test-app";
	private static final String clientName = realmName;
	private static final String userName = "testUser@mail.com";
	private static final String userMail = userName;
	private static final String userPassword = "password";
	private static final String domain = "testDomain.com";
	public static final String NEW = "new";

	@Inject
	private IKeycloakService keycloakService;
	@Inject
	private AliasDao aliasDao;


	@Test
	public void isAssociatedPositiveWithClientNameAtRequest() {
		UUID userId = UUIDs.timeBased();

		init(domain, realmName, clientName, userName, userMail, userPassword, APP_ID, userId);

		final String hostName = clientName + keycloakService.separator() + domain;

		HttpClient httpClient = getHttpClientWithCustomDns(hostName);
		HttpGet httpRequest = new HttpGet("http://" + hostName +":8080/rest/alias/isassociated/" + userName);

		HttpResponse httpResponse;
        AliasServiceImpl.Associated associated = null;
		try {
            httpResponse = httpClient.execute(httpRequest);
			associated = parseResponse(httpResponse, AliasServiceImpl.Associated.class);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertNotNull(associated);
		assertEquals(true, associated.isAssociated());
		assertEquals(clientName + keycloakService.separator() + domain, associated.getSubdomain());
	}

	@Test
	public void isAssociatedPositiveWithoutClientNameAtRequest() {

		UUID userId = UUIDs.timeBased();

		init(domain, realmName, clientName, userName, userMail, userPassword, APP_ID, userId);

		HttpClient httpClient = getHttpClientWithCustomDns(domain);
		HttpGet httpRequest = new HttpGet("http://" + domain +":8080/rest/alias/isassociated/" + userName);

		HttpResponse httpResponse;
		AliasServiceImpl.Associated associated = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
			final Class<AliasServiceImpl.Associated> AssociatedType = AliasServiceImpl.Associated.class;
			associated = parseResponse(httpResponse, AssociatedType);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		assertNotNull(associated);
		assertEquals(true, associated.isAssociated());
		assertEquals(clientName + keycloakService.separator() + domain, associated.getSubdomain());
	}

	@Test
	public void isAssociatedNegativeWithOtherDomain() {
		UUID userId = UUIDs.timeBased();

		init(domain, realmName, clientName, userName, userMail, userPassword, APP_ID, userId);

		final String otherDomain = OTHER + domain;
		final String hostName = clientName + keycloakService.separator() + otherDomain;

		HttpClient httpClient = getHttpClientWithCustomDns(hostName);
		HttpGet httpRequest = new HttpGet("http://" + hostName +":8080/rest/alias/isassociated/" + userName);

		HttpResponse httpResponse;
		AliasServiceImpl.Associated associated = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
			associated = parseResponse(httpResponse, AliasServiceImpl.Associated.class);
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertNotNull(associated);
		assertFalse(associated.isAssociated());
	}

	@Test
	public void isAssociatedNegativeUserInOtherRealm() {

		UUID newAppId = UUIDs.timeBased();
		UUID userId = UUIDs.timeBased();
		UUID newUserId = UUIDs.timeBased();

		String newRealmName = NEW + realmName;
		String newClientName = NEW + clientName;
		String newUserName = NEW + userName;
		String newUserMail = NEW + userMail;
		init(domain, newRealmName, newClientName, newUserName, newUserMail, userPassword, newAppId, newUserId);

		init(domain, realmName, clientName, userName, userMail, userPassword, APP_ID, userId);

		final String hostName = clientName + keycloakService.separator() + domain;

		HttpClient httpClient = getHttpClientWithCustomDns(hostName);
		HttpGet httpRequest = new HttpGet("http://" + hostName +":8080/rest/alias/isassociated/" + newUserName);

		HttpResponse httpResponse;
		AliasServiceImpl.Associated associated = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
			final Class<AliasServiceImpl.Associated> AssociatedType = AliasServiceImpl.Associated.class;
			associated = parseResponse(httpResponse, AssociatedType);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		assertNotNull(associated);
		assertEquals(false, associated.isAssociated());
	}

	@Test
	public void isAssociatedNegativeUserDoesntExist() {

		UUID userId = UUIDs.timeBased();

		init(domain, realmName, clientName, userName, userMail, userPassword, APP_ID, userId);

		HttpClient httpClient = getHttpClientWithCustomDns(domain);
		String notUser = "Not" + userName;
		HttpGet httpRequest = new HttpGet("http://" + domain +":8080/rest/alias/isassociated/" + notUser);

		HttpResponse httpResponse;
		AliasServiceImpl.Associated associated = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
			final Class<AliasServiceImpl.Associated> AssociatedType = AliasServiceImpl.Associated.class;
			associated = parseResponse(httpResponse, AssociatedType);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		assertNotNull(associated);
		assertEquals(false, associated.isAssociated());
	}

	@Test
	public void registeredPositiveWithoutClientNameAtRequest() {
		UUID userId = UUIDs.timeBased();

		init(domain, realmName, clientName, userName, userMail, userPassword, APP_ID, userId);

		HttpClient httpClient = getHttpClientWithCustomDns(domain);
		HttpGet httpRequest = new HttpGet("http://" + domain +":8080/rest/alias/registered/" + userName);

		HttpResponse httpResponse;
		Boolean isRegistered = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
			isRegistered = parseResponse(httpResponse, Boolean.class);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		assertTrue(isRegistered);
	}

	@Test
	public void registeredPositiveWithClientNameAtRequest() {
		UUID userId = UUIDs.timeBased();

		init(domain, realmName, clientName, userName, userMail, userPassword, APP_ID, userId);

		final String hostName = clientName + keycloakService.separator() + domain;

		HttpClient httpClient = getHttpClientWithCustomDns(hostName);
		HttpGet httpRequest = new HttpGet("http://" + hostName +":8080/rest/alias/registered/" + userName);

		HttpResponse httpResponse;
		Boolean isRegistered = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
			isRegistered = parseResponse(httpResponse, Boolean.class);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		assertTrue(isRegistered);
	}

	@Test
	public void registeredNegativeUserDoesntExist() {
		UUID userId = UUIDs.timeBased();

		init(domain, realmName, clientName, userName, userMail, userPassword, APP_ID, userId);

		HttpClient httpClient = getHttpClientWithCustomDns(domain);
		String notUser = "Not" + userName;
		HttpGet httpRequest = new HttpGet("http://" + domain +":8080/rest/alias/registered/" + notUser);

		HttpResponse httpResponse;
		Boolean isRegistered = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
			isRegistered = parseResponse(httpResponse, Boolean.class);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		assertNotNull(isRegistered);
		assertFalse(isRegistered);
	}

	@Test
	public void registeredNegativeUserRegisteredInOtherRealm() {
		UUID newAppId = UUIDs.timeBased();
		UUID userId = UUIDs.timeBased();
		UUID newUserId = UUIDs.timeBased();

		String newRealmName = NEW + realmName;
		String newClientName = NEW + clientName;
		String newUserName = NEW + userName;
		String newUserMail = NEW + userMail;
		init(domain, newRealmName, newClientName, newUserName, newUserMail, userPassword, newAppId, newUserId);

		init(domain, realmName, clientName, userName, userMail, userPassword, APP_ID, userId);

		final String hostName = clientName + keycloakService.separator() + domain;

		HttpClient httpClient = getHttpClientWithCustomDns(hostName);
		HttpGet httpRequest = new HttpGet("http://" + hostName +":8080/rest/alias/registered/" + newUserName);

		HttpResponse httpResponse;
		Boolean isRegistered = null;
		try {
			httpResponse = httpClient.execute(httpRequest);
			isRegistered = parseResponse(httpResponse, Boolean.class);
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		assertNotNull(isRegistered);
		assertFalse(isRegistered);
	}

	private void init(String domain, String realmName, String clientName, String userName, String userMail, String userPassword, UUID appId, UUID userId) {
		System.setProperty(OAuth2Configuration.KEY_OAUTH2_ENFORE_DOMAIN, domain);

		//create realm
		keycloakService.createRealmIfAbsent(realmName);

		//create client
		PushApplication newApplication = new PushApplication();
		newApplication.setName(clientName);
		newApplication.setPushApplicationID(appId.toString());

	 	PushApplication oldApplication = pushApplicationService.findByName(clientName);
		if (oldApplication != null) {
			pushApplicationService.removePushApplication(oldApplication);
		}

		pushApplicationService.addPushApplication(newApplication, new LoggedInUser(userName));
		keycloakService.createClientIfAbsent(newApplication);
		keycloakService.setDirectAccessGrantsEnabled(clientName, realmName, true);

		//create user at newly created realm
		keycloakService.delete(userName, clientName);
		keycloakService.createVerifiedUserIfAbsent(userName, userPassword, Collections.emptyList(), realmName);
		keycloakService.setPasswordUpdateRequired(userName, realmName, false);
		keycloakService.updateUserPassword(userName, userPassword, userPassword, clientName);

		//create user at DB
		String other = "";
		Alias alias = new Alias(UUID.fromString(newApplication.getPushApplicationID()), userId, userMail, other);
		aliasDao.create(alias);
	}

	private <T> T parseResponse(HttpResponse httpResponse, Class<T> associatedType) throws IOException {
		byte[] bytes = new byte[httpResponse.getEntity().getContent().available()];
		httpResponse.getEntity().getContent().read(bytes);
		String stringResponse = new String(bytes);

		Gson gson = new Gson();
		return gson.fromJson(stringResponse, associatedType);
	}

	private HttpClient getHttpClientWithCustomDns(String hostName) {
		DnsResolver dnsResolver = new SystemDefaultDnsResolver() {
			@Override
			public InetAddress[] resolve(final String host) throws UnknownHostException {
				if (host.equalsIgnoreCase(hostName)) {
					/* If we match the host we're trying to talk to,
					   return the IP address we want, not what is in DNS */
					return new InetAddress[] { InetAddress.getByName(LOCALHOST) };
				} else {
					/* Else, resolve it as we would normally */
					return super.resolve(host);
				}
			}
		};

		/* HttpClientConnectionManager allows us to use custom DnsResolver */
		BasicHttpClientConnectionManager connManager = new BasicHttpClientConnectionManager(
				/* We're forced to create a SocketFactory Registry.  Passing null
				   doesn't force a default Registry, so we re-invent the wheel. */
				RegistryBuilder.<ConnectionSocketFactory>create()
						.register("http", PlainConnectionSocketFactory.getSocketFactory())
						.register("https", SSLConnectionSocketFactory.getSocketFactory())
						.build(),
				null, /* Default ConnectionFactory */
				null, /* Default SchemePortResolver */
				dnsResolver  /* Our DnsResolver */
		);

		/* build HttpClient that will use our DnsResolver */
		return HttpClientBuilder.create()
				.setConnectionManager(connManager)
				.build();
	}

	@Test
	public void registerAlias() {
		ResteasyClient client = new ResteasyClientBuilder().build();

		ResteasyWebTarget target = client
				.target(getRestFullPath() + "/alias");

		Alias original = new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Supprot@AeroGear.org");
		// Create Alias
		Response response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS)
				.post(Entity.entity(original, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(200, response.getStatus());
		response.close();

		// Query for previously created alias by alias lower(name)
		target = client.target(getRestFullPath() + "/alias/name/"
				+ original.getEmail().toLowerCase());

		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
		Assert.assertTrue(response.getStatus() == 200);
		Alias alias = response.readEntity(Alias.class);
		Assert.assertTrue(alias != null & alias.getEmail().equals(original.getEmail()));
		response.close();

		// Query for previously created alias by alias id
		target = client
				.target(getRestFullPath() + "/alias/" + original.getId());

		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
		Assert.assertTrue(response.getStatus() == 200);
		alias = response.readEntity(Alias.class);
		Assert.assertTrue(alias != null & alias.getEmail().equals(original.getEmail()));
		response.close();
	}

	@Test
	public void registerAliases() {
		ResteasyWebTarget target = getAllAliasesTarget(getRestFullPath());

		List<Alias> aliases = new ArrayList<>();
		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Supprot@AeroGear.org"));
		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Test@AeroGear.org"));
		aliases.add(new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Help@AeroGear.org"));

		// Create 3 Aliases
		Response response = target.request().post(Entity.entity(aliases, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(200, response.getStatus());
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
			target = getAliasByNameTarget(getRestFullPath(), alias.getEmail());

			response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
			Assert.assertTrue(response.getStatus() == 200);
			Alias aliasObj = response.readEntity(Alias.class);
			Assert.assertTrue(aliasObj != null & aliasObj.getEmail().equals(alias.getEmail()));
			response.close();
		}
	}

	@Test
	public void delete() {
		ResteasyClient client = new ResteasyClientBuilder().build();

		ResteasyWebTarget target = client
				.target(getRestFullPath() + "/alias");

		Alias original = new Alias(UUID.fromString(DEFAULT_APP_ID), UUIDs.timeBased(), "Supprot888@AeroGear.org");
		// Create Alias
		Response response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS)
				.post(Entity.entity(original, MediaType.APPLICATION_JSON_TYPE));

		assertEquals(200, response.getStatus());
		response.close();

		// Query for previously created alias by alias lower(name)
		target = client.target(getRestFullPath() + "/alias/name/"
				+ original.getEmail().toLowerCase());

		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
		Assert.assertTrue(response.getStatus() == 200);
		Alias alias = response.readEntity(Alias.class);
		Assert.assertTrue(alias != null & alias.getEmail().equals(original.getEmail()));
		Assert.assertTrue(alias != null & alias.getId().equals(original.getId()));
		response.close();

		// Delete alias
		target = client
				.target(getRestFullPath() + "/alias/" + alias.getId());
		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).delete();
		Assert.assertTrue(response.getStatus() == 200);
		response.close();

		// Query for previously deleted alias by alias id
		target = client.target(getRestFullPath() + "/alias/" + original.getId());

		response = HttpBasicHelper.basic(target.request(), DEFAULT_APP_ID, DEFAULT_APP_PASS).get();
		Assert.assertTrue(response.getStatus() == 200);
		Assert.assertTrue(!response.hasEntity());
		response.close();
	}
}