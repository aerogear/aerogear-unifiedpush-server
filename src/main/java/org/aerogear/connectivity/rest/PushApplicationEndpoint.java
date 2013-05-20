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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

    
    
    
    
    @Resource(mappedName = "java:/ConnectionFactory")
    private ConnectionFactory connectionFactory;
    @Resource(mappedName = "java:/topic/aerogear/pushApp")
    private Topic pushAppTopic;
    
    Connection connection = null;
    Session session = null;
    
    
//    public PushApplicationEndpoint() {
//       try {
//        connection = connectionFactory.createConnection();
//        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
//        connection.start();
//       } catch (JMSException e) {
//           e.printStackTrace();
//       }
//    }
//    
    // ===============================================================
    // =============== Push Application construct ====================
    // ===============================================================
    
    
    // CREATE
    @POST
    @Consumes("application/json")
    public PushApplication registerPushApplication(PushApplication pushApp) {
        // create ID...
        
        pushApp.setId(UUID.randomUUID().toString());
        
        
        
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(pushAppTopic);
            connection.start();
            
            ObjectMessage om = session.createObjectMessage(pushApp);
            messageProducer.send(om);
            
        } catch (JMSException e) {
            e.printStackTrace();
        }
        
        
      return pushAppService.addPushApplication(pushApp);
    }

    // READ
    @GET
    @Produces("application/json")
    public List<PushApplication> listAllPushApplications()  {
        return pushAppService.findAllPushApplications();
    }
    @GET
    @Path("/{pushAppID}")
    @Produces("application/json")
    public PushApplication findById(@PathParam("pushAppID") String id) {
        return pushAppService.findPushApplicationById(id);
    }

    // UPDATE
    @PUT
    @Path("/{pushAppID}")
    @Consumes("application/json")
    public PushApplication updatePushApplication(@PathParam("pushAppID") String id, PushApplication updatedPushApp) {
        PushApplication pushApp = pushAppService.findPushApplicationById(id);
        
        if (pushApp != null) {
            
            // update name/desc:
            pushApp.setDescription(updatedPushApp.getDescription());
            pushApp.setName(updatedPushApp.getName());
            return pushAppService.updatePushApplication(pushApp);
        }

        return pushApp;
    }

    // DELETE
    @DELETE
    @Path("/{pushAppID}")
    @Consumes("application/json")
    public void deletePushApplication(@PathParam("pushAppID") String id) {
        PushApplication pushApp = pushAppService.findPushApplicationById(id);
        
        if (pushApp != null)
            pushAppService.removePushApplication(pushApp);
    }
    
    
    // ===============================================================
    // =============== Mobile variant construct ======================
    // ===============           iOS            ======================
    // ===============================================================
    // new iOS
    @POST
    @Path("/{pushAppID}/iOS")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public iOSApplication registeriOSVariant(
            @MultipartForm iOSApplicationUploadForm form, 
            @PathParam("pushAppID") String pushApplicationId) {

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
    // READ
    @GET
    @Path("/{pushAppID}/iOS")
    @Produces("application/json")
    public Set<iOSApplication> listAlliOSVariationsForPushApp(@PathParam("pushAppID") String pushAppID)  {
        PushApplication pushApp = pushAppService.findPushApplicationById(pushAppID);
        if (pushApp != null) {
            return pushApp.getIOSApps();
        }
        return Collections.emptySet();
    }
    @GET
    @Path("/{pushAppID}/iOS/{iOSID}")
    @Produces("application/json")
    public iOSApplication findiOSVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("iOSID") String iOSID) {
        return iOSappService.findiOSApplicationById(iOSID);
    }
    // UPDATE
    @PUT
    @Path("/{pushAppID}/iOS/{iOSID}")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public iOSApplication updateiOSVariant(
            @MultipartForm iOSApplicationUploadForm form, 
            @PathParam("pushAppID") String pushApplicationId,
            @PathParam("iOSID") String iOSID) {
        
        iOSApplication iOSVariation = iOSappService.findiOSApplicationById(iOSID);
        if (iOSVariation != null) {
            // apply update:
            iOSVariation.setName(form.getName());
            iOSVariation.setDescription(form.getDescription());
            iOSVariation.setPassphrase(form.getPassphrase());
            iOSVariation.setCertificate(form.getCertificate());

            iOSappService.updateiOSApplication(iOSVariation);
        }
        return iOSVariation;
    }
    
    // DELETE
    @DELETE
    @Path("/{pushAppID}/iOS/{iOSID}")
    @Consumes("application/json")
    public void deleteiOSVariation(@PathParam("pushAppID") String id, @PathParam("iOSID") String iOSID) {
        iOSApplication iOSVariation = iOSappService.findiOSApplicationById(iOSID);
        
        if (iOSVariation != null)
            iOSappService.removeiOSApplication(iOSVariation);
    }

    

    // ===============================================================
    // =============== Mobile variant construct ======================
    // ===============         Android          ======================
    // ===============================================================
   // new Android
   @POST
   @Path("/{pushAppID}/android")
   @Consumes("application/json")
   public AndroidApplication registerAndroidVariant(AndroidApplication androidVariation, @PathParam("pushAppID") String pushApplicationId) {
       // store the Android variant:
       androidVariation = androidAppService.addAndroidApplication(androidVariation);
       // find the root push app
       PushApplication pushApp = pushAppService.findPushApplicationById(pushApplicationId);
       // add iOS variant, and merge:
       pushAppService.addAndroidApplication(pushApp, androidVariation);

       return androidVariation;
   }
   // READ
   @GET
   @Path("/{pushAppID}/android")
   @Produces("application/json")
   public Set<AndroidApplication> listAllAndroidVariationsForPushApp(@PathParam("pushAppID") String pushAppID)  {
       PushApplication pushApp = pushAppService.findPushApplicationById(pushAppID);
       if (pushApp != null) {
           return pushApp.getAndroidApps();
       }
       return Collections.emptySet();
   }
   @GET
   @Path("/{pushAppID}/android/{androidID}")
   @Produces("application/json")
   public AndroidApplication findAndroidVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("androidID") String androidID) {
       return androidAppService.findAndroidApplicationById(androidID);
   }
   // UPDATE
   @PUT
   @Path("/{pushAppID}/android/{androidID}")
   @Consumes("application/json")
   public AndroidApplication updateAndroidVariation(
           @PathParam("pushAppID") String id,
           @PathParam("androidID") String androidID,
           AndroidApplication updatedAndroidApplication) {
       
       
       AndroidApplication androidVariant = androidAppService.findAndroidApplicationById(androidID);
       if (androidVariant != null) {
           
           // apply updated data:
           androidVariant.setGoogleKey(updatedAndroidApplication.getGoogleKey());
           androidVariant.setName(updatedAndroidApplication.getName());
           androidVariant.setDescription(updatedAndroidApplication.getDescription());
           return androidAppService.updateAndroidApplication(androidVariant);
       }

       return androidVariant;
   }
   // DELETE
   @DELETE
   @Path("/{pushAppID}/android/{androidID}")
   @Consumes("application/json")
   public void deleteAndroidVariation(@PathParam("pushAppID") String id, @PathParam("androidID") String androidID) {
       AndroidApplication androidVariant = androidAppService.findAndroidApplicationById(androidID);
       
       if (androidVariant != null)
           androidAppService.removeAndroidApplication(androidVariant);
   }
   
   
   
   // ===============================================================
   // =============== Mobile variant construct ======================
   // ===============        SimplePush        ======================
   // ===============================================================
   
   

   // new SimplePush
   @POST
   @Path("/{pushAppID}/simplePush")
   @Consumes("application/json")
   public SimplePushApplication registerSimplePushVariant(SimplePushApplication spa, @PathParam("pushAppID") String pushApplicationId) {
       // store the SimplePush variant:
       spa = simplePushApplicationService.addSimplePushApplication(spa);
       // find the root push app
       PushApplication pushApp = pushAppService.findPushApplicationById(pushApplicationId);
       // add iOS variant, and merge:
       pushAppService.addSimplePushApplication(pushApp, spa);
       return spa;
   }
   // READ
   @GET
   @Path("/{pushAppID}/simplePush")
   @Produces("application/json")
   public Set<SimplePushApplication> listAllSimplePushVariationsForPushApp(@PathParam("pushAppID") String pushAppID)  {
       PushApplication pushApp = pushAppService.findPushApplicationById(pushAppID);
       if (pushApp != null) {
           return pushApp.getSimplePushApps();
       }
       return Collections.emptySet();
   }
   @GET
   @Path("/{pushAppID}/simplePush/{simplePushID}")
   @Produces("application/json")
   public SimplePushApplication findSimplePushVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("simplePushID") String simplePushID) {
       return simplePushApplicationService.findSimplePushApplicationById(simplePushID);
   }
   // UPDATE
   @PUT
   @Path("/{pushAppID}/simplePush/{simplePushID}")
   @Consumes("application/json")
   public SimplePushApplication updateSimplePushVariation(
           @PathParam("pushAppID") String id,
           @PathParam("simplePushID") String simplePushID,
           SimplePushApplication updatedSimplePushApplication) {
       
       SimplePushApplication spVariant = simplePushApplicationService.findSimplePushApplicationById(simplePushID);
       if (spVariant != null) {
           
           // apply updated data:
           spVariant.setName(updatedSimplePushApplication.getName());
           spVariant.setDescription(updatedSimplePushApplication.getDescription());
           spVariant.setPushNetworkURL(updatedSimplePushApplication.getPushNetworkURL());
           return simplePushApplicationService.updateSimplePushApplication(spVariant);
       }

       return spVariant;
   }
   // DELETE
   @DELETE
   @Path("/{pushAppID}/simplePush/{simplePushID}")
   @Consumes("application/json")
   public void deleteSimplePushVariation(@PathParam("pushAppID") String id, @PathParam("simplePushID") String simplePushID) {
       SimplePushApplication spVariant = simplePushApplicationService.findSimplePushApplicationById(simplePushID);
       if (spVariant != null) 
           simplePushApplicationService.removeSimplePushApplication(spVariant);
   }

   
}