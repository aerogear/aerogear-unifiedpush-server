package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.unifiedpush.api.WindowsVariant;
import org.jboss.aerogear.unifiedpush.api.WindowsWNSVariant;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.Response;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WindowsVariantEndpointTest {
    WindowsVariantEndpoint endpoint;

    @Mock
    PushApplicationService pushAppService;

    @Mock
    GenericVariantService variantService;

    @Before
    public void before() {
        this.endpoint = new WindowsVariantEndpoint();

        this.endpoint.pushAppService = pushAppService;
        this.endpoint.variantService = variantService;
    }

    @Test
    public void shouldFindVariantById() {
        // base cases for findVariantById of WindowsVariantEndpoint is
        // tested in AndroidVariantEndpointTest

        final WindowsVariant variant = new WindowsWNSVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.findVariantById("123");
        assertEquals(response.getStatus(), 200);
        assertTrue(variant == response.getEntity());    // identity check
    }

    @Test
    public void shouldDeleteVariant() {
        // base cases for deleteVariant of WindowsVariantEndpoint is
        // tested in AndroidVariantEndpointTest

        final WindowsVariant variant = new WindowsWNSVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.deleteVariant("123");
        assertEquals(response.getStatus(), 204);

        verify(variantService).removeVariant(variant);
    }

    @Test
    public void shouldResetSecret() {
        // base cases for resetSecret of WindowsVariantEndpoint is
        // tested in AndroidVariantEndpointTest

        final WindowsVariant variant = new WindowsWNSVariant();
        variant.setSecret("The Secret");

        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.resetSecret("123");
        assertEquals(response.getStatus(), 200);

        verify(variantService).updateVariant(variant);
        assertNotEquals("The Secret", variant.getSecret());
    }

}