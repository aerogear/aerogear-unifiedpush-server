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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.aerogear.connectivity.api.MobileApplication;
import org.aerogear.connectivity.model.MobileApplicationInstance;
import org.aerogear.connectivity.service.MobileApplicationInstanceService;
import org.aerogear.connectivity.service.MobileApplicationService;

@Stateless
@Path("/registry/device")
@TransactionAttribute
public class MobileApplicationInstanceEndpoint
{
    @Inject
    private Logger logger;
    
    @Inject
    private MobileApplicationInstanceService mobileApplicationInstanceService;

    @Inject
    private MobileApplicationService mobileApplicationService;

    @POST
    @Consumes("application/json")
    public MobileApplicationInstance registerInstallation(
            @HeaderParam("ag-mobile-app") String mobileVariantID, 
            MobileApplicationInstance entity) {
        if (logger.isLoggable(Level.INFO)) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n\nag-mobile-app: ");
            sb.append(mobileVariantID);
            sb.append("\n\n");
            logger.info(sb.toString());
        }
        
        // set the ID:
        entity.setId(UUID.randomUUID().toString());

        // store the installation:
        entity = mobileApplicationInstanceService.addMobileApplicationInstance(entity);
        // find the matching variation:
        MobileApplication mobileApp = mobileApplicationService.findMobileApplicationById(mobileVariantID);
        
        if (mobileApp == null) {
            logger.severe("\n\nCould not find Mobile Variant\n\n");
            return null; // TODO -> 404 ... or even 500 ?
        }

        // add installation to the matching variant
        mobileApplicationService.addInstallation(mobileApp, entity);

        return entity;
   }


    @PUT
    @Path("{token}")
    @Consumes("application/json")
    public MobileApplicationInstance updateInstance(
            @HeaderParam("ag-mobile-app") String mobileVariantID, 
            @PathParam("token") String token,
            MobileApplicationInstance postedVariant) {

        // there can be multiple regs.........
        List<MobileApplicationInstance> instances = mobileApplicationInstanceService.findMobileApplicationInstancesByToken(token);

        // TODO: make sure there is really just one 
        MobileApplicationInstance mvi = instances.get(0);
        
        mvi.setCategory(postedVariant.getCategory());
        mvi.setDeviceToken(postedVariant.getDeviceToken());
        mvi.setClientIdentifier(postedVariant.getClientIdentifier());
        mvi.setDeviceType(postedVariant.getDeviceType());
        mvi.setMobileOperatingSystem(postedVariant.getMobileOperatingSystem());
        mvi.setOsVersion(postedVariant.getOsVersion());

        //update
        mobileApplicationInstanceService.updateMobileApplicationInstance(mvi);
        
        return mvi;
    }
    
    @DELETE
    @Path("{token}")
    @Consumes("application/json")
    public void unregisterInstallations(
            @HeaderParam("ag-mobile-app") String mobileVariantID, 
            @PathParam("token") String token) {
        
        // there can be multiple regs.........
        List<MobileApplicationInstance> instances = mobileApplicationInstanceService.findMobileApplicationInstancesByToken(token);
        
        // delete them:
        mobileApplicationInstanceService.removeMobileApplicationInstances(instances);
   }
}