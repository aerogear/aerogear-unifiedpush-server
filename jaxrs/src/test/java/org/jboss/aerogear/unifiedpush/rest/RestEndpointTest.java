package org.jboss.aerogear.unifiedpush.rest;

import java.net.URL;

import javax.ws.rs.ApplicationPath;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.rest.util.Authenticator;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public abstract class RestEndpointTest {
	protected static final String RESOURCE_PREFIX = RestApplication.class.getAnnotation(ApplicationPath.class).value()
			.substring(1);

	protected static final String DEFAULT_APP_ID = "7385c294-2003-4abf-83f6-29a27415326b";
	protected static final String DEFAULT_APP_PASS = "1f73558f-d01b-48d7-9f6a-fc5d0ec6da51";

	protected static final String DEFAULT_IOS_VARIENT_ID = "d3f54c25-c3ce-4999-b7a8-27dc9bb01364";
	protected static final String DEFAULT_IOS_VARIENT_PASS = "088a814a-ff2b-4acf-9091-5bcd0ccece16";
	protected static final String DEFAULT_IOS_DEVICE_TOKEN = "c5106a4e97ecc8b8ab8448c2ebccbfa25938c0f9a631f96eb2dd5f16f0bedc40";

	protected static final String DEFAULT_AND_VARIENT_ID = "55b2c428-7102-43f7-96c0-96b5c7ba8dcc";
	protected static final String DEFAULT_AND_VARIENT_PASS = "20aba35f-e472-4958-9f8f-f55f73e2e012";
	protected static final String DEFAULT_AND_DEVICE_TOKEN = "eHlfnI0__dI:APA91bEhtHefML2lr_sBQ-bdXIyEn5owzkZg_p_y7SRyNKRMZ3XuzZhBpTOYIh46tqRYQIc-7RTADk4nM5H-ONgPDWHodQDS24O5GuKP8EZEKwNh4Zxdv1wkZJh7cU2PoLz9gn4Nxqz-";

	protected static final String DEFAULT_IOS_DEVICE_ALIAS = "Support@tEst.com";
	protected static final String DEFAULT_AND_DEVICE_ALIAS = "android@test.com";

	protected ResteasyClient iosClient = new ResteasyClientBuilder()
			.register(new Authenticator(DEFAULT_IOS_VARIENT_ID, DEFAULT_IOS_VARIENT_PASS)).build();

	protected ResteasyClient androidClient = new ResteasyClientBuilder()
			.register(new Authenticator(DEFAULT_AND_VARIENT_ID, DEFAULT_AND_VARIENT_PASS)).build();

	protected ResteasyClient applicationClient = new ResteasyClientBuilder()
			.register(new Authenticator(DEFAULT_APP_ID, DEFAULT_APP_PASS)).build();

	protected static Installation getIosDefaultInstallation() {
		Installation iosInstallation = new Installation();
		iosInstallation.setDeviceType("iPhone7,2");
		iosInstallation.setDeviceToken(DEFAULT_IOS_DEVICE_TOKEN);
		iosInstallation.setOperatingSystem("iOS");
		iosInstallation.setOsVersion("9.0.2");
		iosInstallation.setAlias(DEFAULT_IOS_DEVICE_ALIAS);

		return iosInstallation;
	}

	protected static Installation getAndroidDefaultInstallation() {
		Installation iosInstallation = new Installation();
		iosInstallation.setDeviceType("Galaxy S7");
		iosInstallation.setDeviceToken(DEFAULT_AND_DEVICE_TOKEN);
		iosInstallation.setOperatingSystem("Android");
		iosInstallation.setOsVersion("6.0.1");
		iosInstallation.setAlias(DEFAULT_AND_DEVICE_ALIAS);

		return iosInstallation;
	}

	protected ResteasyWebTarget getAllAliasesTarget(URL deploymentUrl) {
		return applicationClient.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/alias/all");
	}

	/*
	 * Return ResteasyWebTarget authenticated with DEFAULT_APP_ID/DEFAULT_APP_PASS
	 */
	protected ResteasyWebTarget getAliasByNameTarget(URL deploymentUrl, String alias) {
		return applicationClient.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/alias/name/" + alias);
	}
}
