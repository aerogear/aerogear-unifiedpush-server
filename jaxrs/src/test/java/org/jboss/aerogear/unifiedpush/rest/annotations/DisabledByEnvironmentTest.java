package org.jboss.aerogear.unifiedpush.rest.annotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.rest.filter.DisabledVariantEndpointFilter;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.AndroidVariantEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.WebPushVariantEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.iOSTokenVariantEndpoint;
import org.jboss.aerogear.unifiedpush.rest.registry.applications.iOSVariantEndpoint;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DisabledByEnvironmentTest {

    private DisabledVariantEndpointFilter filter;

    @Before
    public void setup() throws ServletException {
        System.setProperty("UPS_DISABLED", "ios,android,webpush,web_push,iostoken,ios_token");
        filter = new DisabledVariantEndpointFilter();
    }


    @Test
    public void doNotAbortIfNotInVariable() throws IOException, ServletException {
        System.setProperty("UPS_DISABLED", "");
        DisabledVariantEndpointFilter localFilter = new DisabledVariantEndpointFilter();
        ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/rest/applications/fake-push-id/android", "",
                "");
        uriInfo.pushCurrentResource(new AndroidVariantEndpoint());
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getUriInfo()).thenReturn(uriInfo);
        doThrow(new RuntimeException("This should not be aborted.")).when(context).abortWith(Matchers.any());
        
        try {
            localFilter.filter(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void androidEnvDisablesAndroid() throws IOException {
        ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/rest/applications/fake-push-id/android", "",
                "");
        uriInfo.pushCurrentResource(new AndroidVariantEndpoint());
        runTest(uriInfo);
    }

    @Test
    public void iosTokenEnvDisablesiOSToken() {
        ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/rest/applications/fake-push-id/ios_token", "",
                "");
        uriInfo.pushCurrentResource(new iOSTokenVariantEndpoint());
        runTest(uriInfo);

        uriInfo = new ResteasyUriInfo("http://example.org/rest/applications/fake-push-id/iostoken", "", "");
        uriInfo.pushCurrentResource(new iOSTokenVariantEndpoint());
        runTest(uriInfo);

    }

    private void runTest(ResteasyUriInfo uriInfo) {
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getUriInfo()).thenReturn(uriInfo);
        ArgumentCaptor<Response> argument = ArgumentCaptor.forClass(Response.class);

        try {
            filter.filter(context);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        verify(context).abortWith(argument.capture());

        assertEquals(404, argument.getValue().getStatus());
    }

    @Test
    public void iosEnvDisablesIOS() {
        ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/rest/applications/fake-push-id/ios", "", "");
        uriInfo.pushCurrentResource(new iOSVariantEndpoint());
        runTest(uriInfo);
    }

    @Test
    public void webPushEnvDisablesWebPush() {
        ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/rest/applications/fake-push-id/webpush", "",
                "");
        uriInfo.pushCurrentResource(new WebPushVariantEndpoint());
        runTest(uriInfo);

        uriInfo = new ResteasyUriInfo("http://example.org/rest/applications/fake-push-id/web_push", "", "");
        uriInfo.pushCurrentResource(new WebPushVariantEndpoint());
        runTest(uriInfo);
    }

}
