package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import java.net.URL;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.rest.RestApplication;
import org.jboss.aerogear.unifiedpush.rest.util.Authenticator;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.Configuration;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PropertyPlaceholderConfigurer;
import org.jboss.aerogear.unifiedpush.test.archive.UnifiedPushArchive;
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

@RunWith(Arquillian.class)
public class InstallationRegistrationEndpointTest {
	private static final String RESOURCE_PREFIX = RestApplication.class.getAnnotation(ApplicationPath.class).value().substring(1);
	
	private static final String  DEFAULT_VARIENT_ID = "d3f54c25-c3ce-4999-b7a8-27dc9bb01364";
	private static final String  DEFAULT_VARIENT_PASS = "088a814a-ff2b-4acf-9091-5bcd0ccece16";
	private static final String  DEFAULT_DEVICE_TOKEN = "c5106a4e97ecc8b8ab8448c2ebccbfa25938c0f9a631f96eb2dd5f16f0bedc40";
	
    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(InstallationRegistrationEndpointTest.class)
        		.addMavenDependencies("org.jboss.aerogear.unifiedpush:unifiedpush-service")
        		.addAsLibrary("org.jboss.aerogear.unifiedpush:unifiedpush-model-jpa", new String[]{"META-INF/persistence.xml", 
        				"test-data.sql"}, new String[] {"META-INF/test-persistence.xml", "META-INF/test-data.sql"})
                .addPackage(RestApplication.class.getPackage())
                .addPackage(InstallationRegistrationEndpoint.class.getPackage())
                .addClasses(InstallationRegistrationEndpoint.class, RestApplication.class, HttpBasicHelper.class, Authenticator.class)
                .addAsWebInfResource("META-INF/test-ds.xml", "test-ds.xml")
                .addAsResource("default.properties")
                .as(WebArchive.class);
    }
    
    @BeforeClass
    public static void initResteasyClient() {
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
    }
    
    @Test
    @RunAsClient
    public void unAuthorizedDeviceTest(@ArquillianResource URL deploymentUrl) {
    	ResteasyClient client = new ResteasyClientBuilder().build();
    
    	ResteasyWebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/registry/device");
    	Response response = target.request().post(Entity.entity(new Installation(), MediaType.APPLICATION_JSON_TYPE));
    	
    	Assert.assertTrue(response.getStatus() == 401);
    }
    
    @Test
    @RunAsClient
    public void registerDeviceTest(@ArquillianResource URL deploymentUrl) {
    	// Prepare installation 
    	Installation iosInstallation = new Installation();
    	iosInstallation.setDeviceType("iPhone7,2");
    	iosInstallation.setDeviceToken(DEFAULT_DEVICE_TOKEN);
    	iosInstallation.setOperatingSystem("iOS");
    	iosInstallation.setOsVersion("9.0.2");
    	iosInstallation.setAlias("17327572923");
    	
    	ResteasyClient client = new ResteasyClientBuilder().register(new Authenticator(DEFAULT_VARIENT_ID, DEFAULT_VARIENT_PASS)).build();
    	
    	try{
	    	ResteasyWebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/registry/device");
	    	Response response = target.request().post(Entity.entity(iosInstallation, MediaType.APPLICATION_JSON_TYPE));
	    	Installation newInstallation = response.readEntity(Installation.class);
	    
			Assert.assertTrue(response.getStatus() == 200);
			Assert.assertTrue(newInstallation.isEnabled());
    	} catch (Throwable e){
    		Assert.fail(e.getMessage());
    	}
    }

    @Inject 
    private Configuration configuration;
    @Inject
    private GenericVariantService genericVariantService;
    @Inject
    private ClientInstallationService installationService;
    
    
    @Test
    public void enableDeviceTest() {
    	// Prepare installation 
    	Installation iosInstallation = new Installation();
    	iosInstallation.setDeviceType("iPhone7,2");
    	iosInstallation.setDeviceToken(DEFAULT_DEVICE_TOKEN);
    	iosInstallation.setOperatingSystem("iOS");
    	iosInstallation.setOsVersion("9.0.2");
    	iosInstallation.setAlias("17327572923");
    	
    	try {    	
			configuration.setSystemPropertiesMode(PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_OVERRIDE);
			System.setProperty(Configuration.PROP_ENABLE_VERIFICATION, "true");
			
			Variant variant = genericVariantService.findByVariantID(DEFAULT_VARIENT_ID);
			Assert.assertTrue(variant.getVariantID().equals(DEFAULT_VARIENT_ID));
			
			installationService.addInstallation(variant, iosInstallation);
			
			// Wait for @Asynchronous EJB to finish
			Thread.sleep(500);
			Installation inst = installationService.findById(iosInstallation.getId());
			
			Assert.assertTrue(inst != null && inst.isEnabled() == false);
		} catch (Throwable e) {
			Assert.fail(e.getMessage());
		}
    }
}
