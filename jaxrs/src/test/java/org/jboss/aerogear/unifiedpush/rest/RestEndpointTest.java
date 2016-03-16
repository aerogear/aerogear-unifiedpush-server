package org.jboss.aerogear.unifiedpush.rest;

import org.jboss.aerogear.unifiedpush.api.Installation;

public abstract class RestEndpointTest {

	protected static final String  DEFAULT_VARIENT_ID = "d3f54c25-c3ce-4999-b7a8-27dc9bb01364";
	protected static final String  DEFAULT_VARIENT_PASS = "088a814a-ff2b-4acf-9091-5bcd0ccece16";
	protected static final String  DEFAULT_DEVICE_TOKEN = "c5106a4e97ecc8b8ab8448c2ebccbfa25938c0f9a631f96eb2dd5f16f0bedc40";

	protected static Installation getDefaultInstallation(){
    	Installation iosInstallation = new Installation();
    	iosInstallation.setDeviceType("iPhone7,2");
    	iosInstallation.setDeviceToken(DEFAULT_DEVICE_TOKEN);
    	iosInstallation.setOperatingSystem("iOS");
    	iosInstallation.setOsVersion("9.0.2");
    	iosInstallation.setAlias("17327572923");

    	return iosInstallation;
	}
}
