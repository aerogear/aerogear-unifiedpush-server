/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import com.qmino.miredot.annotations.BodyType;
import com.qmino.miredot.annotations.ReturnType;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.jboss.aerogear.unifiedpush.api.WindowsMPNSVariant;
import org.jboss.aerogear.unifiedpush.api.WindowsVariant;
import org.jboss.aerogear.unifiedpush.api.WindowsWNSVariant;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.aerogear.unifiedpush.rest.AbstractBaseEndpoint;
import org.jboss.aerogear.unifiedpush.rest.util.BootstrapForm;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;
import org.jboss.aerogear.unifiedpush.service.PushApplicationService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

@Path("/applications/bootstrap")
public class BootstrapEndpoint extends AbstractBaseEndpoint {

    @Inject
    private PushApplicationService pushAppService;

    @Inject
    protected GenericVariantService variantService;

    /**
     * Convenience endpoint to create a complete Push Application with a set of variants
     *
     * @param form              Form containing data for Push App and variants
     * @return                  created {@link PushApplication}
     *
     * @statuscode 201 The PushApplication with Variants are created successfully
     * @statuscode 400 The format of the client request was incorrect
     */
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @BodyType("org.jboss.aerogear.unifiedpush.rest.util.BootstrapForm")
    @ReturnType("org.jboss.aerogear.unifiedpush.api.PushApplication")
    public Response registerPushApplicationAndVariants(@MultipartForm BootstrapForm form) {

        // some basic validation
        try {
            validateModelClass(form);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            Response.ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // root push application
        final PushApplication pushApplication = new PushApplication();
        pushApplication.setName(form.getPushApplicationName());
        pushAppService.addPushApplication(pushApplication);


        // nested variants
        AndroidVariant androidVariant;
        iOSVariant iOSVariant;
        WindowsVariant windowsVariant = null;

        // Android around ?
        if (form.getAndroidVariantName() != null) {
            androidVariant = new AndroidVariant();
            androidVariant.setName(form.getAndroidVariantName());
            androidVariant.setGoogleKey(form.getAndroidGoogleKey());
            androidVariant.setProjectNumber(form.getAndroidProjectNumber());

            // store the model, add variant references and merge:
            variantService.addVariant(androidVariant);
            pushAppService.addVariant(pushApplication, androidVariant);
        }

        // iOS around ?
        if (form.getiOSVariantName() != null) {
            iOSVariant = new iOSVariant();
            iOSVariant.setName(form.getiOSVariantName());
            iOSVariant.setPassphrase(form.getiOSPassphrase());
            iOSVariant.setCertificate(form.getiOSCertificate());
            iOSVariant.setProduction(form.getiOSProduction());

            // store the model, add variant references and merge:
            variantService.addVariant(iOSVariant);
            pushAppService.addVariant(pushApplication, iOSVariant);
        }

        //Windows around?
        if (form.getWindowsVariantName() != null) {

            final String windowsType = form.getWindowsType().toLowerCase();

            switch (windowsType) {
                case "mpns":
                    WindowsMPNSVariant mpnsVariant = new WindowsMPNSVariant();
                    mpnsVariant.setName(form.getWindowsVariantName());

                    // store ref:
                    windowsVariant = mpnsVariant;

                    break;
                case "wns":
                    WindowsWNSVariant wnsVariant = new WindowsWNSVariant();
                    wnsVariant.setName(form.getWindowsVariantName());
                    wnsVariant.setSid(form.getWindowsSid());
                    wnsVariant.setClientSecret(form.getWindowsClientSecret());

                    // store ref:
                    windowsVariant = wnsVariant;

                    break;
            }

            // store the model, add variant references and merge:
            variantService.addVariant(windowsVariant);
            pushAppService.addVariant(pushApplication, windowsVariant);

        }

        return Response.created(
                UriBuilder.fromResource(PushApplicationEndpoint.class)
                        .path(String.valueOf(pushApplication.getPushApplicationID()))
                        .build())
                .entity(pushApplication)
                .build();
    }
}
