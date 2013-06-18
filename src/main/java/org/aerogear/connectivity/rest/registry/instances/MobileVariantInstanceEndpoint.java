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

package org.aerogear.connectivity.rest.registry.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.aerogear.connectivity.api.MobileVariant;
import org.aerogear.connectivity.model.MobileVariantInstanceImpl;
import org.aerogear.connectivity.rest.security.util.HttpBasicHelper;
import org.aerogear.connectivity.service.MobileVariantInstanceService;
import org.aerogear.connectivity.service.MobileVariantService;

@Stateless
@Path("/registry/device")
@TransactionAttribute
public class MobileVariantInstanceEndpoint
{
    @Inject private Logger logger;
    @Inject private MobileVariantInstanceService mobileApplicationInstanceService;
    @Inject private MobileVariantService mobileApplicationService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response registerInstallation(
            MobileVariantInstanceImpl entity,
            @Context HttpServletRequest request) {
        
        System.out.println("\n\n\n SSSSSSSSSS");

        // find the matching variation:
        final MobileVariant mobileVariant = loadMobileVariantWhenAuthorized(request);
        if (mobileVariant == null) {
            return Response.status(Status.UNAUTHORIZED).entity("Unauthorized Request").build();
        }
            
        // Poor validation: We require the Token!
        if(entity.getDeviceToken() == null) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        // look up all instances for THIS variant:
        List<MobileVariantInstanceImpl> instances = findInstanceByDeviceToken(mobileVariant.getInstances(), entity.getDeviceToken());
        if (instances.isEmpty()) {
            // store the installation:
            entity = mobileApplicationInstanceService.addMobileVariantInstance(entity);
            // add installation to the matching variant
            mobileApplicationService.addInstance(mobileVariant, entity);
        } else {
            logger.info("Updating received metadata for MobileVariantInstance");

            // should be impossible
            if (instances.size()>1) {
                logger.severe("Too many registration for one installation");
            }

            // update the entity:
            entity = this.updateMobileApplicationInstance(instances.get(0), entity);
        }

        return Response.ok().build();
   }

    @DELETE
    @Path("{token}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response unregisterInstallations(
            @PathParam("token") String token,
            @Context HttpServletRequest request) {

        // find the matching variation:
        final MobileVariant mobileVariant = loadMobileVariantWhenAuthorized(request);
        if (mobileVariant == null) {
            return Response.status(Status.UNAUTHORIZED).entity("Unauthorized Request").build();
        }

        // there can be multiple regs.........
        List<MobileVariantInstanceImpl> instances = mobileApplicationInstanceService.findMobileVariantInstancesByToken(token);
        // delete them:
        mobileApplicationInstanceService.removeMobileVariantInstances(instances);

        return Response.noContent().build();
   }
    
    // TODO: move to JQL
    private List<MobileVariantInstanceImpl> findInstanceByDeviceToken(Set<MobileVariantInstanceImpl> instances, String deviceToken) {
        final List<MobileVariantInstanceImpl> instancesWithToken = new ArrayList<MobileVariantInstanceImpl>();

        for (MobileVariantInstanceImpl instance : instances) {
            if (instance.getDeviceToken().equals(deviceToken))
                instancesWithToken.add(instance);
        }

        return instancesWithToken;
    }

    private MobileVariantInstanceImpl updateMobileApplicationInstance(MobileVariantInstanceImpl toUpdate, MobileVariantInstanceImpl postedVariant) {
        toUpdate.setCategory(postedVariant.getCategory());
        toUpdate.setDeviceToken(postedVariant.getDeviceToken());
        toUpdate.setAlias(postedVariant.getAlias());
        toUpdate.setDeviceType(postedVariant.getDeviceType());
        toUpdate.setMobileOperatingSystem(postedVariant.getMobileOperatingSystem());
        toUpdate.setOsVersion(postedVariant.getOsVersion());

        //update
        return mobileApplicationInstanceService.updateMobileVariantInstance(toUpdate);
    }

    /**
     * returns application if the masterSecret is valid for the request PushApplication
     */
    private MobileVariant loadMobileVariantWhenAuthorized(HttpServletRequest request) {
        // extract the pushApplicationID and its secret from the HTTP Basic header:
        String[] credentials = HttpBasicHelper.extractUsernameAndPasswordFromBasicHeader(request);
        String mobileVariantID = credentials[0];
        String secret = credentials[1];
        System.out.println("\n\nmobileVariantID: " + mobileVariantID);
        System.out.println("\n\nsecret: " + secret);
        
        
        final MobileVariant mobileVariant = mobileApplicationService.findByVariantID(mobileVariantID);
        if (mobileVariant != null && mobileVariant.getSecret().equals(secret)) {
            return mobileVariant;
        }

        // unauthorized...
        return null;
    }
}