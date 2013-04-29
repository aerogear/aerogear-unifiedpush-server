/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.aerogear.connectivity.rest;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.aerogear.connectivity.model.AndroidApplication;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.model.SimplePushApplication;
import org.aerogear.connectivity.model.iOSApplication;
import org.aerogear.connectivity.rest.util.iOSApplicationUploadForm;
import org.aerogear.connectivity.service.AndroidApplicationService;
import org.aerogear.connectivity.service.PushApplicationService;
import org.aerogear.connectivity.service.SimplePushApplicationService;
import org.aerogear.connectivity.service.iOSApplicationService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

@Stateless
@Path("/applications")
@TransactionAttribute
public class PushApplicationEndpoint {
   
    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private iOSApplicationService iOSappService;
    @Inject
    private AndroidApplicationService androidAppService;
    @Inject
    private SimplePushApplicationService simplePushApplicationService;
   
    @GET
    @Produces("application/json")
    public List<PushApplication> listAllPushApplications()  {
        return pushAppService.findAllPushApplications();
    }

//    @GET
//    @Path("/{id:[0-9][0-9]*}")
//    @Produces("application/json")
//    public PushApplication findById(@PathParam("id") String id) {
//      //return em.find(PushApplication.class, id);
//        return pushAppService.findPushApplicationById(id);
//    }

    @POST
    @Consumes("application/json")
    public PushApplication registerPushApplication(PushApplication pushApp) {
      return pushAppService.addPushApplication(pushApp);
    }
   
    // new iOS
    @POST
    @Path("/{id}/iOS")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public iOSApplication registeriOSVariant(
            @MultipartForm iOSApplicationUploadForm form, 
            @PathParam("id") String pushApplicationId) {

        // extract form values:
        iOSApplication iOSVariation = new iOSApplication();
        iOSVariation.setName(form.getName());
        iOSVariation.setDescription(form.getDescription());
        iOSVariation.setPassphrase(form.getPassphrase());
        iOSVariation.setCertificate(form.getCertificate());
                
        // store the iOS variant:
        iOSVariation = iOSappService.addiOSApplication(iOSVariation);

        // find the root push app
        PushApplication pushApp = pushAppService.findPushApplicationById(pushApplicationId);
        // add iOS variant, and merge:
        pushAppService.addiOSApplication(pushApp, iOSVariation);

        return iOSVariation;
   }

   
   // new Android
   @POST
   @Path("/{id}/android")
   @Consumes("application/json")
   public AndroidApplication registerAndroidVariant(AndroidApplication androidVariation, @PathParam("id") String pushApplicationId) {
       // store the Android variant:
       androidVariation = androidAppService.addAndroidApplication(androidVariation);
       // find the root push app
       PushApplication pushApp = pushAppService.findPushApplicationById(pushApplicationId);
       // add iOS variant, and merge:
       pushAppService.addAndroidApplication(pushApp, androidVariation);

       return androidVariation;
   }

   // new SimplePush
   @POST
   @Path("/{id}/simplePush")
   @Consumes("application/json")
   public SimplePushApplication registerSimplePushVariant(SimplePushApplication spa, @PathParam("id") String pushApplicationId) {
       // store the SimplePush variant:
       spa = simplePushApplicationService.addSimplePushApplication(spa);
       // find the root push app
       PushApplication pushApp = pushAppService.findPushApplicationById(pushApplicationId);
       // add iOS variant, and merge:
       pushAppService.addSimplePushApplication(pushApp, spa);
       return spa;
   }
}