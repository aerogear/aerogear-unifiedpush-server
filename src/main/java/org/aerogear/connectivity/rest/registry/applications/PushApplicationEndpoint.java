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

import java.util.List;
import java.util.UUID;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.aerogear.connectivity.model.PushApplication;
import org.aerogear.connectivity.service.PushApplicationService;

@Stateless
@TransactionAttribute
@Path("/applications")
public class PushApplicationEndpoint extends AbstractRegistryEndpoint {

    @Inject
    private PushApplicationService pushAppService;
    @Inject Event<PushApplication> pushApplicationEventSource;
 
    // CREATE
    @POST
    @Consumes("application/json")
    //@Asynchronous
    public PushApplication registerPushApplication(PushApplication pushApp) {
        // create ID...
        pushApp.setId(UUID.randomUUID().toString());

        // delegate:
        //pushAppService.addPushApplication(pushApp);
        publishPushApplication(pushApp);

        return pushApp;
    }
    
    @Asynchronous
    public void publishPushApplication(PushApplication pushApp) {
        //pushAppService.addPushApplication(pushApp);
        
        pushApplicationEventSource.fire(pushApp);
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
}