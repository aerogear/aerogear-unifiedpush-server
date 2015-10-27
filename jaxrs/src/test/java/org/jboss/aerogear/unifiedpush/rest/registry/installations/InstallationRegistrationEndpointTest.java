package org.jboss.aerogear.unifiedpush.rest.registry.installations;

import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.api.Installation;
import org.jboss.aerogear.unifiedpush.rest.RestApplication;
import org.jboss.aerogear.unifiedpush.rest.util.HttpBasicHelper;
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
@RunAsClient
public class InstallationRegistrationEndpointTest {
	private static final String RESOURCE_PREFIX = RestApplication.class.getAnnotation(ApplicationPath.class).value().substring(1);
	
    @Deployment
    public static WebArchive archive() {
        return UnifiedPushArchive.forTestClass(InstallationRegistrationEndpointTest.class)
        		.addMavenDependencies("org.jboss.aerogear.unifiedpush:unifiedpush-service")
        		.addAsLibrary("org.jboss.aerogear.unifiedpush:unifiedpush-model-jpa", "META-INF/persistence.xml", "META-INF/test-persistence.xml")
                .addPackage(RestApplication.class.getPackage())
                .addPackage(InstallationRegistrationEndpoint.class.getPackage())
                .addClasses(InstallationRegistrationEndpoint.class, RestApplication.class, HttpBasicHelper.class)
                .addAsWebInfResource("META-INF/test-ds.xml", "test-ds.xml")
                .as(WebArchive.class);
    }
    
    @ArquillianResource
    URL deploymentUrl;
    
    @Context HttpServletRequest request;
    
    @BeforeClass
    public static void initResteasyClient() {
        RegisterBuiltin.register(ResteasyProviderFactory.getInstance());
    }
    
    @Test
    public void unAuthorizedDeviceTest() {
    	ResteasyClient client = new ResteasyClientBuilder().build();

    	ResteasyWebTarget target = client.target(deploymentUrl.toString() + RESOURCE_PREFIX + "/registry/device");
    	Response response = target.request().post(Entity.entity(new Installation(), "application/json"));
    	
    	Assert.assertTrue(response.getStatus() == 401);
    }
}
