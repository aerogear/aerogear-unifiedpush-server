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

package org.aerogear.connectivity.rest.registry.applications;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.aerogear.connectivity.model.AndroidVariant;
import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.service.AndroidVariantService;
import org.aerogear.connectivity.service.PushApplicationService;

@Stateless
@TransactionAttribute
@Path("/applications/{pushAppID}/android")
public class AndroidVariantEndpoint {
    
    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private AndroidVariantService androidAppService;
   

    // ===============================================================
    // =============== Mobile variant construct ======================
    // ===============         Android          ======================
    // ===============================================================
   // new Android
   @POST
   @Consumes("application/json")
   public AndroidVariant registerAndroidVariant(AndroidVariant androidVariation, @PathParam("pushAppID") String pushApplicationID) {
       // manually set the ID:
       androidVariation.setVariantID(UUID.randomUUID().toString());
       
       
       // delegate down:
       // store the Android variant:
       androidVariation = androidAppService.addAndroidVariant(androidVariation);
       // find the root push app
       PushApplication pushApp = pushAppService.findByPushApplicationID(pushApplicationID);
       // add iOS variant, and merge:
       pushAppService.addAndroidVariant(pushApp, androidVariation);

       return androidVariation;
   }

   // READ
   @GET
   @Produces("application/json")
   public Set<AndroidVariant> listAllAndroidVariationsForPushApp(@PathParam("pushAppID") String pushAppID)  {
       PushApplication pushApp = pushAppService.findByPushApplicationID(pushAppID);
       if (pushApp != null) {
           return pushApp.getAndroidApps();
       }
       return Collections.emptySet();
   }
   @GET
   @Path("/{androidID}")
   @Produces("application/json")
   public AndroidVariant findAndroidVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("androidID") String androidID) {
       return androidAppService.findByVariantID(androidID);
   }
   // UPDATE
   @PUT
   @Path("/{androidID}")
   @Consumes("application/json")
   public AndroidVariant updateAndroidVariation(
           @PathParam("pushAppID") String id,
           @PathParam("androidID") String androidID,
           AndroidVariant updatedAndroidApplication) {
       
       
       AndroidVariant androidVariant = androidAppService.findByVariantID(androidID);
       if (androidVariant != null) {
           
           // apply updated data:
           androidVariant.setGoogleKey(updatedAndroidApplication.getGoogleKey());
           androidVariant.setName(updatedAndroidApplication.getName());
           androidVariant.setDescription(updatedAndroidApplication.getDescription());
           return androidAppService.updateAndroidVariant(androidVariant);
       }

       return androidVariant;
   }
   // DELETE
   @DELETE
   @Path("/{androidID}")
   @Consumes("application/json")
   public void deleteAndroidVariation(@PathParam("pushAppID") String id, @PathParam("androidID") String androidID) {
       AndroidVariant androidVariant = androidAppService.findByVariantID(androidID);
       
       if (androidVariant != null)
           androidAppService.removeAndroidVariant(androidVariant);
   }
  
}