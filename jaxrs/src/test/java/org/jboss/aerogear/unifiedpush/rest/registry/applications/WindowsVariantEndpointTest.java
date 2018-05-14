package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.unifiedpush.api.*;
import org.jboss.aerogear.unifiedpush.api.WindowsVariant;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.aerogear.unifiedpush.service.PushSearchService;
import org.jboss.aerogear.unifiedpush.service.impl.SearchManager;
import org.jboss.resteasy.spi.ResteasyUriInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.Validator;
import javax.ws.rs.core.Response;

import java.util.Collections;

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

    @Mock
    SearchManager searchManager;

    @Mock
    PushSearchService searchService;

    @Mock
    Validator validator;

    @Before
    public void before() {
        this.endpoint = new WindowsVariantEndpoint(validator, searchManager);

        this.endpoint.pushAppService = pushAppService;
        this.endpoint.variantService = variantService;

        when(searchManager.getSearchService()).thenReturn(searchService);
    }

    @Test
    public void shouldRegisterWindowsVariantSuccessfully() {
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/abc", "", "push");
        final PushApplication pushApp = new PushApplication();

        final WindowsVariant variant = new WindowsWNSVariant();
        variant.setName("variant name");

        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(pushApp);
        when(validator.validate(variant)).thenReturn(Collections.EMPTY_SET);

        final Response response = this.endpoint.registerWindowsVariant(variant, "push-app-id", uriInfo);

        assertEquals(response.getStatus(), 201);
        final WindowsVariant createdVariant = (WindowsVariant) response.getEntity();
        assertEquals(createdVariant.getName(), "variant name");
        assertEquals(response.getMetadata().get("location").get(0).toString(), "http://example.org/abc/" + createdVariant.getVariantID());

        verify(variantService).addVariant(createdVariant);
        verify(pushAppService).addVariant(pushApp, createdVariant);
    }

    @Test
    public void shouldUpdateWindowsVariantSuccessfully() {
        final WindowsWNSVariant original = new WindowsWNSVariant();

        final WindowsWNSVariant update = new WindowsWNSVariant();
        update.setName("variant name");
        update.setClientSecret("client secret");

        when(variantService.findByVariantID("variant-id")).thenReturn(original);
        when(validator.validate(update)).thenReturn(Collections.EMPTY_SET);

        final Response response = this.endpoint.updateWindowsVariant("variant-id", update);

        assertEquals(response.getStatus(), 200);
        assertEquals(original.getName(), "variant name");
        assertEquals(original.getClientSecret(), "client secret");

        verify(variantService).updateVariant(original);
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