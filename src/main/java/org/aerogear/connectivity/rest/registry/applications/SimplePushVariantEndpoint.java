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
import org.aerogear.connectivity.model.SimplePushApplication;
import org.aerogear.connectivity.service.PushApplicationService;
import org.aerogear.connectivity.service.SimplePushApplicationService;

@Stateless
@TransactionAttribute
@Path("/applications/{pushAppID}/simplePush")
public class SimplePushVariantEndpoint extends AbstractRegistryEndpoint {

    @Inject
    private PushApplicationService pushAppService;
    @Inject
    private SimplePushApplicationService simplePushApplicationService;

   // ===============================================================
   // =============== Mobile variant construct ======================
   // ===============        SimplePush        ======================
   // ===============================================================
   
   // new SimplePush
   @POST
   @Consumes("application/json")
   public SimplePushApplication registerSimplePushVariant(SimplePushApplication spa, @PathParam("pushAppID") String pushApplicationId) {
       // manually set the ID:
       spa.setId(UUID.randomUUID().toString());
       
       try {
           connection = connectionFactory.createConnection();
           session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
           MessageProducer messageProducer = session.createProducer(pushAppTopic);
           connection.start();
           
           ObjectMessage om = session.createObjectMessage(spa);
           om.setStringProperty("ApplicationType", "aerogear.SimplePushApplication");
           om.setStringProperty("PushApplicationID", pushApplicationId);
           messageProducer.send(om);

           session.close();
           connection.close();

       } catch (JMSException e) {
           e.printStackTrace();
       }
       return spa;
   }
   // READ
   @GET
   @Produces("application/json")
   public Set<SimplePushApplication> listAllSimplePushVariationsForPushApp(@PathParam("pushAppID") String pushAppID)  {
       PushApplication pushApp = pushAppService.findPushApplicationById(pushAppID);
       if (pushApp != null) {
           return pushApp.getSimplePushApps();
       }
       return Collections.emptySet();
   }
   @GET
   @Path("/{simplePushID}")
   @Produces("application/json")
   public SimplePushApplication findSimplePushVariationById(@PathParam("pushAppID") String pushAppID, @PathParam("simplePushID") String simplePushID) {
       return simplePushApplicationService.findSimplePushApplicationById(simplePushID);
   }
   // UPDATE
   @PUT
   @Path("/{simplePushID}")
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
   @Path("/{simplePushID}")
   @Consumes("application/json")
   public void deleteSimplePushVariation(@PathParam("pushAppID") String id, @PathParam("simplePushID") String simplePushID) {
       SimplePushApplication spVariant = simplePushApplicationService.findSimplePushApplicationById(simplePushID);
       if (spVariant != null) 
           simplePushApplicationService.removeSimplePushApplication(spVariant);
   }

   
}