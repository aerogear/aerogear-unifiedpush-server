package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.iOSTokenVariant;
import org.jboss.aerogear.unifiedpush.message.jms.APNSClientProducer;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class iOSTokenVariantEndpointTest {
    iOSTokenVariantEndpoint endpoint;

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

    @Mock
    APNSClientProducer producer;

    @Before
    public void before() {
        this.endpoint = new iOSTokenVariantEndpoint(validator, searchManager);

        this.endpoint.pushAppService = pushAppService;
        this.endpoint.variantService = variantService;
        this.endpoint.producer = producer;

        when(searchManager.getSearchService()).thenReturn(searchService);
    }

    @Test
    public void shouldRegisteriOSTokenVariantSuccessfully() {
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/abc", "", "push");
        final PushApplication pushApp = new PushApplication();

        final iOSTokenVariant form = new iOSTokenVariant();
        form.setName("variant name");
        form.setPrivateKey("privateKey");
        form.setProduction(false);

        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(pushApp);
        when(validator.validate(form)).thenReturn(Collections.EMPTY_SET);

        final Response response = this.endpoint.registeriOSVariant(form, "push-app-id", uriInfo);

        assertEquals(response.getStatus(), 201);
        final iOSTokenVariant createdVariant = (iOSTokenVariant) response.getEntity();
        assertEquals(createdVariant.getName(), "variant name");
        assertEquals(response.getMetadata().get("location").get(0).toString(), "http://example.org/abc/" + createdVariant.getVariantID());

        verify(variantService).addVariant(createdVariant);
        verify(pushAppService).addVariant(pushApp, createdVariant);
    }

    @Test
    public void shouldUpdateiOSTokenVariantSuccessfully_withMultiPartData() {
        final iOSTokenVariant original = new iOSTokenVariant();

        final iOSTokenVariant update = new iOSTokenVariant();
        update.setName("variant name");
        update.setPrivateKey("privateKey");
        update.setProduction(false);

        when(variantService.findByVariantID("variant-id")).thenReturn(original);
        when(validator.validate(update)).thenReturn(Collections.EMPTY_SET);
        when(validator.validate(original)).thenReturn(Collections.EMPTY_SET);

        final Response response = this.endpoint.updateiOSVariant(update, "push-app-id", "variant-id");

        assertEquals(response.getStatus(), 200);
        assertEquals(original.getName(), "variant name");

        verify(variantService).updateVariant(original);
        verify(producer).changeAPNClient(original);
    }

    @Test
    public void shouldUpdateiOSTokenVariantSuccessfully_noMultiPartData() {
        final iOSTokenVariant original = new iOSTokenVariant();

        final iOSTokenVariant update = new iOSTokenVariant();
        update.setName("variant name");
        update.setPrivateKey("certificate");
        update.setProduction(false);

        when(variantService.findByVariantID("variant-id")).thenReturn(original);

        final Response response = this.endpoint.updateiOSVariant("push-app-id", "variant-id", update);

        assertEquals(response.getStatus(), 204);
        assertEquals(original.getName(), "variant name");

        verify(variantService).updateVariant(original);
    }

    @Test
    public void shouldFindVariantById() {
        // base cases for findVariantById of iOSTokenVariantEndpoint is
        // tested in AndroidVariantEndpointTest

        final iOSTokenVariant variant = new iOSTokenVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.findVariantById("123");
        assertEquals(response.getStatus(), 200);
        assertTrue(variant == response.getEntity());    // identity check
    }

    @Test
    public void shouldDeleteVariant() {
        // base cases for deleteVariant of iOSTokenVariantEndpoint is
        // tested in AndroidVariantEndpointTest

        final iOSTokenVariant variant = new iOSTokenVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.deleteVariant("123");
        assertEquals(response.getStatus(), 204);

        verify(variantService).removeVariant(variant);
    }

    @Test
    public void shouldResetSecret() {
        // base cases for resetSecret of iOSTokenVariantEndpoint is
        // tested in AndroidVariantEndpointTest

        final iOSTokenVariant variant = new iOSTokenVariant();
        variant.setSecret("The Secret");

        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.resetSecret("123");
        assertEquals(response.getStatus(), 200);

        verify(variantService).updateVariant(variant);
        assertNotEquals("The Secret", variant.getSecret());
    }
}