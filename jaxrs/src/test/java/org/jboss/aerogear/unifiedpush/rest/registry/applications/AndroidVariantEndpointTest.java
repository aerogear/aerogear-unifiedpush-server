package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
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
public class AndroidVariantEndpointTest {

    AndroidVariantEndpoint endpoint;

    @Mock
    PushApplicationService pushAppService;

    @Mock
    GenericVariantService variantService;

    @Before
    public void before() {
        this.endpoint = new AndroidVariantEndpoint();

        this.endpoint.pushAppService = pushAppService;
        this.endpoint.variantService = variantService;
    }


    @Test
    public void shouldReturn404WhenFindVariantByIdCannotFindAnyVariants() {
        when(variantService.findByVariantID("foo")).thenReturn(null);

        final Response response = this.endpoint.findVariantById("foo");
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldReturn400WhenFindVariantByIdFindsVariantOfAnotherPlatform() {
        when(variantService.findByVariantID("123")).thenReturn(new iOSVariant());

        final Response response = this.endpoint.findVariantById("123");
        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void shouldReturn200WhenFindVariantByIdFindsAnAndroidVariant() {
        final AndroidVariant variant = new AndroidVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.findVariantById("123");
        assertEquals(response.getStatus(), 200);
        assertTrue(variant == response.getEntity());    // identity check
    }

    @Test
    public void shouldReturn404WhenDeleteVariantCannotFindAnyVariants() {
        when(variantService.findByVariantID("foo")).thenReturn(null);

        final Response response = this.endpoint.deleteVariant("foo");
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldReturn400WhenDeleteVariantFindsVariantOfAnotherPlatform() {
        when(variantService.findByVariantID("123")).thenReturn(new iOSVariant());

        final Response response = this.endpoint.deleteVariant("123");
        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void shouldReturn204WhenDeleteVariantDeletesAnAndroidVariant() {
        final AndroidVariant variant = new AndroidVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.deleteVariant("123");
        assertEquals(response.getStatus(), 204);

        verify(variantService).removeVariant(variant);
    }

    @Test
    public void shouldReturn404WhenResetSecretCannotFindAnyVariants() {
        when(variantService.findByVariantID("foo")).thenReturn(null);

        final Response response = this.endpoint.resetSecret("foo");
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldReturn400WhenResetSecretFindsVariantOfAnotherPlatform() {
        when(variantService.findByVariantID("123")).thenReturn(new iOSVariant());

        final Response response = this.endpoint.resetSecret("123");
        assertEquals(response.getStatus(), 400);
    }

    @Test
    public void shouldReturn204WhenResetSecretFindsAnAndroidVariant() {
        final AndroidVariant variant = new AndroidVariant();
        variant.setSecret("The Secret");

        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.resetSecret("123");
        assertEquals(response.getStatus(), 200);

        verify(variantService).updateVariant(variant);
        assertNotEquals("The Secret", variant.getSecret());
    }
}