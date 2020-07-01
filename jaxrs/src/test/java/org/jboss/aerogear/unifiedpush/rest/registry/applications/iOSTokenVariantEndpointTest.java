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
    private static final String GOOD_APP_ID = "good_app_id";
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
        when(searchService.findByPushApplicationIDForDeveloper(GOOD_APP_ID)).thenReturn(new PushApplication());
    }

    @Test
    public void shouldRegisteriOSTokenVariantSuccessfully() {
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/abc", "", "push");
        final PushApplication pushApp = new PushApplication();

        final iOSTokenVariant form = new iOSTokenVariant();
        form.setName("variant name");
        form.setPrivateKey("privateKey");
        form.setTeamId("team id");
        form.setKeyId("key id");
        form.setBundleId("test.my.toe");
        form.setProduction(false);

        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(pushApp);
        when(validator.validate(form)).thenReturn(Collections.EMPTY_SET);

        final Response response = this.endpoint.registeriOSVariant(form, "push-app-id", uriInfo);

        assertEquals(response.getStatus(), 201);
        final iOSTokenVariant createdVariant = (iOSTokenVariant) response.getEntity();
        assertEquals(createdVariant.getName(), "variant name");
        assertEquals(createdVariant.getPrivateKey(), "privateKey");
        assertEquals(createdVariant.getTeamId(), "team id");
        assertEquals(createdVariant.getKeyId(), "key id");
        assertEquals(createdVariant.getBundleId(), "test.my.toe");


        assertEquals(response.getMetadata().get("location").get(0).toString(), "http://example.org/abc/" + createdVariant.getVariantID());

        verify(variantService).addVariant(createdVariant);
        verify(pushAppService).addVariant(pushApp, createdVariant);
    }

    @Test
    public void shouldUpdateiOSTokenVariantSuccessfully_withMultiPartData() {
        final iOSTokenVariant original = new iOSTokenVariant();
        final iOSTokenVariant update = new iOSTokenVariant();
        final PushApplication pushApp = new PushApplication();
        update.setName("variant name");
        update.setPrivateKey("privateKey");
        update.setProduction(false);

        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(pushApp);
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
        final PushApplication pushApp = new PushApplication();

        update.setName("variant name");
        update.setPrivateKey("certificate");
        update.setTeamId("team id");
        update.setKeyId("key id");
        update.setBundleId("test.my.toe");


        update.setProduction(false);


        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(pushApp);
        when(variantService.findByVariantID("variant-id")).thenReturn(original);

        final Response response = this.endpoint.updateiOSVariant("push-app-id", "variant-id", update);

        assertEquals(response.getStatus(), 204);
        assertEquals(original.getName(), "variant name");
        assertEquals(original.getPrivateKey(), "certificate");
        assertEquals(original.getTeamId(), "team id");
        assertEquals(original.getKeyId(), "key id");
        assertEquals(original.getBundleId(), "test.my.toe");


        verify(variantService).updateVariant(original);
    }

    @Test
    public void shouldFindVariantById() {
        // base cases for findVariantById of iOSTokenVariantEndpoint is
        // tested in AndroidVariantEndpointTest

        final iOSTokenVariant variant = new iOSTokenVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.findVariantById(GOOD_APP_ID,"123");
        assertEquals(response.getStatus(), 200);
        assertTrue(variant == response.getEntity());    // identity check
    }

    @Test
    public void shouldDeleteVariant() {
        // base cases for deleteVariant of iOSTokenVariantEndpoint is
        // tested in AndroidVariantEndpointTest

        final iOSTokenVariant variant = new iOSTokenVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.deleteVariant(GOOD_APP_ID,"123");
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

        final Response response = this.endpoint.resetSecret(GOOD_APP_ID,"123");
        assertEquals(response.getStatus(), 200);

        verify(variantService).updateVariant(variant);
        assertNotEquals("The Secret", variant.getSecret());
    }
}