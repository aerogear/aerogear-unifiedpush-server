package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.Variant;
import org.jboss.aerogear.unifiedpush.api.WebPushVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
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

import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.Validator;
import javax.validation.metadata.ConstraintDescriptor;
import javax.ws.rs.core.Response;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@RunWith(MockitoJUnitRunner.class)
public class WebPushVariantEndpointTest {

    private static final String GOOD_APP_ID = "good_app_id";
    WebPushVariantEndpoint endpoint;


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

    ConstraintViolation sampleViolation = new WebPushVariantEndpointTest.StubConstraintViolation();

    @Before
    public void before() {
        this.endpoint = new WebPushVariantEndpoint(validator, searchManager);

        this.endpoint.pushAppService = pushAppService;
        this.endpoint.variantService = variantService;
        when(searchService.findByPushApplicationIDForDeveloper(GOOD_APP_ID)).thenReturn(new PushApplication());
        when(searchManager.getSearchService()).thenReturn(searchService);
    }

    @Test
    public void shouldRegisterWebPushVariantSuccessfully() {
        final WebPushVariant variant = new WebPushVariant();
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/abc", "", "push");
        final PushApplication pushApp = new PushApplication();

        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(pushApp);
        when(validator.validate(variant)).thenReturn(Collections.EMPTY_SET);

        final Response response = this.endpoint.registerWebPushVariant(variant, "push-app-id", uriInfo);

        assertEquals(response.getStatus(), 201);
        assertTrue(response.getEntity() == variant);        // identity check
        assertEquals(response.getMetadata().get("location").get(0).toString(), "http://example.org/abc/" + variant.getVariantID());

        verify(variantService).addVariant(variant);
        verify(pushAppService).addVariant(pushApp, variant);
    }


    @Test
    public void registerWebPushVariantShouldReturn404WhenPushAppDoesNotExist() {
        final WebPushVariant variant = new WebPushVariant();
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/abc", "", "push");
        final PushApplication pushApp = new PushApplication();

        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(null);

        final Response response = this.endpoint.registerWebPushVariant(variant, "push-app-id", uriInfo);

        assertEquals(response.getStatus(), 404);

        verify(variantService, never()).addVariant(any());
        verify(validator, never()).validate(any());
        verify(pushAppService, never()).addVariant(any(), any());
    }

    @Test
    public void registerWebPushVariantShouldReturn400WhenVariantModelIsNotValid() {
        final WebPushVariant variant = new WebPushVariant();
        final ResteasyUriInfo uriInfo = new ResteasyUriInfo("http://example.org/abc", "", "push");
        final PushApplication pushApp = new PushApplication();

        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(pushApp);
        when(validator.validate(variant)).thenReturn(Sets.newHashSet(sampleViolation));

        final Response response = this.endpoint.registerWebPushVariant(variant, "push-app-id", uriInfo);

        assertEquals(response.getStatus(), 400);

        verify(variantService, never()).addVariant(variant);
        verify(pushAppService, never()).addVariant(pushApp, variant);
    }

    @Test
    public void shouldListAllAndroidVariationsForPushAppSuccessfully() {
        final WebPushVariant webPushVariant = new WebPushVariant();
        final Variant iOSVariant = new iOSVariant();
        final PushApplication pushApp = new PushApplication();
        pushApp.setVariants(Lists.newArrayList(webPushVariant, iOSVariant));

        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(pushApp);

        final Response response = this.endpoint.listAllWebPushVariationsForPushApp("push-app-id");

        assertEquals(response.getStatus(), 200);
        assertTrue(((Collection) response.getEntity()).iterator().next() == webPushVariant);        // identity check
    }

    @Test
    public void shouldUpdateWebPushVariantSuccessfully() {
        final WebPushVariant originalVariant = new WebPushVariant();
        final WebPushVariant updatedVariant = new WebPushVariant();
        final PushApplication pushApp = new PushApplication();

        originalVariant.setPublicKey("test public Key");
        updatedVariant.setPublicKey("update public Key");

        when(searchService.findByPushApplicationIDForDeveloper("push-app-id")).thenReturn(pushApp);
        when(variantService.findByVariantID("variant-id")).thenReturn(originalVariant);
        when(validator.validate(updatedVariant)).thenReturn(Collections.EMPTY_SET);

        final Response response = this.endpoint.updateWebPushVariant("push-app-id", "variant-id", updatedVariant);

        assertEquals(response.getStatus(), 200);
        assertTrue(response.getEntity() == originalVariant);        // identity check
        assertEquals(originalVariant.getPublicKey(), "update public Key");

        verify(variantService).updateVariant(originalVariant);
    }

    @Test
    public void shouldReturn404WhenFindVariantByIdCannotFindAnyVariants() {
        when(variantService.findByVariantID("foo")).thenReturn(null);

        final Response response = this.endpoint.findVariantById("","foo");
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldReturn404WhenFindVariantByIdFindsVariantOfAnotherPlatform() {
        when(variantService.findByVariantID("123")).thenReturn(new iOSVariant());

        final Response response = this.endpoint.findVariantById(GOOD_APP_ID,"123");
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldReturn200WhenFindVariantByIdFindsAnWebPushVariant() {
        final WebPushVariant variant = new WebPushVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.findVariantById(GOOD_APP_ID,"123");
        assertEquals(response.getStatus(), 200);
        assertTrue(variant == response.getEntity());    // identity check
    }

    @Test
    public void shouldReturn404WhenDeleteVariantCannotFindAnyVariants() {
        when(variantService.findByVariantID("foo")).thenReturn(null);

        final Response response = this.endpoint.deleteVariant(GOOD_APP_ID,"foo");
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldReturn404WhenDeleteVariantFindsVariantOfAnotherPlatform() {
        when(variantService.findByVariantID("123")).thenReturn(new iOSVariant());

        final Response response = this.endpoint.deleteVariant(GOOD_APP_ID,"123");
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldReturn204WhenDeleteVariantDeletesAnWebPushVariant() {
        final WebPushVariant variant = new WebPushVariant();
        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.deleteVariant(GOOD_APP_ID,"123");
        assertEquals(response.getStatus(), 204);

        verify(variantService).removeVariant(variant);
    }

    @Test
    public void shouldReturn404WhenResetSecretCannotFindAnyVariants() {
        when(variantService.findByVariantID("foo")).thenReturn(null);

        final Response response = this.endpoint.resetSecret(GOOD_APP_ID,"foo");
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldReturn404WhenResetSecretFindsVariantOfAnotherPlatform() {
        when(variantService.findByVariantID("123")).thenReturn(new iOSVariant());

        final Response response = this.endpoint.resetSecret(GOOD_APP_ID,"123");
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void shouldReturn204WhenResetSecretFindsAnWebPushVariant() {
        final WebPushVariant variant = new WebPushVariant();
        variant.setSecret("The Secret");

        when(variantService.findByVariantID("123")).thenReturn(variant);

        final Response response = this.endpoint.resetSecret(GOOD_APP_ID,"123");
        assertEquals(response.getStatus(), 200);

        verify(variantService).updateVariant(variant);
        assertNotEquals("The Secret", variant.getSecret());
    }

    // stub class to mock constraint violation
    private static class StubConstraintViolation implements ConstraintViolation {

        public String getMessage() {
            return "stub violation message";
        }

        public Path getPropertyPath() {
            return new Path() {
                @Override
                public Iterator<Node> iterator() {
                    return null;
                }

                @Override
                public String toString() {
                    return "stub violation path";
                }
            };
        }

        public String getMessageTemplate() {
            return null;
        }

        public Object getRootBean() {
            return null;
        }

        public Class getRootBeanClass() {
            return null;
        }

        public Object getLeafBean() {
            return null;
        }

        public Object getInvalidValue() {
            return null;
        }

        public ConstraintDescriptor<?> getConstraintDescriptor() {
            return null;
        }
    }

}
