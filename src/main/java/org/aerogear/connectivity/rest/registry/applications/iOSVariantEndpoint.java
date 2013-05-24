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
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.model.iOSApplication;
import org.aerogear.connectivity.rest.util.iOSApplicationUploadForm;
import org.aerogear.connectivity.service.PushApplicationService;
import org.aerogear.connectivity.service.iOSApplicationService;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

@Stateless
@TransactionAttribute
@Path("/applications/{pushAppID}/iOS")
public class iOSVariantEndpoint extends AbstractRegistryEndpoint {
    
    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private iOSApplicationService iOSappService;
   
    
    // ===============================================================
    // =============== Mobile variant construct ======================
    // ===============           iOS            ======================
    // ===============================================================
    // new iOS
    @POST
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
        
        // manually set the ID:
        iOSVariation.setId(UUID.randomUUID().toString());
        
        
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(pushAppTopic);
            connection.start();
            
            ObjectMessage om = session.createObjectMessage(iOSVariation);
            om.setStringProperty("ApplicationType", "aerogear.iOSApplication");
            om.setStringProperty("PushApplicationID", pushApplicationId);
            messageProducer.send(om);
            
            session.close();
            connection.close();
            
        } catch (JMSException e) {
            e.printStackTrace();
        }

        return iOSVariation;
   }
    // READ
    @GET
    @Produces("application/json")
    public Set<iOSApplication> listAlliOSVariationsForPushApp(@PathParam("pushAppID") String pushAppID)  {
        PushApplication pushApp = pushAppService.findPushApplicationById(pushAppID);
        if (pushApp != null) {
            return pushApp.getIOSApps();
        }
        return Collections.emptySet();
    }
    @GET
    @Path("/{iOSID}")
    @Produces("application/json")
    public iOSApplication findiOSVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("iOSID") String iOSID) {
        return iOSappService.findiOSApplicationById(iOSID);
    }
    // UPDATE
    @PUT
    @Path("/{iOSID}")
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
    @Path("/{iOSID}")
    @Consumes("application/json")
    public void deleteiOSVariation(@PathParam("pushAppID") String id, @PathParam("iOSID") String iOSID) {
        iOSApplication iOSVariation = iOSappService.findiOSApplicationById(iOSID);
        
        if (iOSVariation != null)
            iOSappService.removeiOSApplication(iOSVariation);
    }
  
}