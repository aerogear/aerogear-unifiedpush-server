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
package org.jboss.aerogear.connectivity.rest.registry.applications;

import org.jboss.aerogear.connectivity.api.MobileVariant;
import org.jboss.aerogear.connectivity.model.MobileVariantInstanceImpl;
import org.jboss.aerogear.connectivity.service.MobileVariantInstanceService;
import org.jboss.aerogear.connectivity.service.MobileVariantService;
import org.jboss.aerogear.security.authz.Secure;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Stateless
@TransactionAttribute
@Path("/applications/{variantID}/instances/")
@Secure("developer")
public class VariantInstanceEndpoint {

    @Inject
    private MobileVariantService mobileApplicationService;

    @Inject
    private MobileVariantInstanceService mobileApplicationInstanceService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findVariantInstances(@PathParam("variantID") String variantId){

        //Find the variant using the variantID
        MobileVariant mobileVariant =  mobileApplicationService.findByVariantID(variantId);

        if(mobileVariant == null){
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Mobile Variant").build();
        }

        return Response.ok(mobileVariant.getInstances()).build();
    }

    @GET
    @Path("/{instanceID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findVariantInstances(@PathParam("variantID") String variantId, @PathParam("instanceID") String instanceId){

        //Find the variant using the variantID
        MobileVariant mobileVariant =  mobileApplicationService.findByVariantID(variantId);

        if(mobileVariant == null){
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Mobile Variant").build();
        }

        List<MobileVariantInstanceImpl> mobileVariantInstance =  findInstanceById(mobileVariant.getInstances(), instanceId);

        if(mobileVariantInstance.size() == 0){
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Mobile Variant Instance").build();
        }

        return Response.ok(mobileVariantInstance.get(0)).build();
    }

    @PUT
    @Path("/{instanceID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateVariantInstance(MobileVariantInstanceImpl entity, @PathParam("variantID") String variantId, @PathParam("instanceID") String instanceId){

        //Find the variant using the variantID
        MobileVariant mobileVariant =  mobileApplicationService.findByVariantID(variantId);

        if(mobileVariant == null){
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Mobile Variant").build();
        }

        List<MobileVariantInstanceImpl> mobileVariantInstance =  findInstanceById(mobileVariant.getInstances(), instanceId);

        if(mobileVariantInstance.size() == 0){
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Mobile Variant Instance").build();
        }

        updateMobileApplicationInstance(entity, mobileVariantInstance.get(0));

        return Response.noContent().build();

    }

    @DELETE
    @Path("/{instanceID}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeVariantInstance(@PathParam("variantID") String variantId, @PathParam("instanceID") String instanceId){

        //Find the variant using the variantID
        MobileVariant mobileVariant =  mobileApplicationService.findByVariantID(variantId);

        if(mobileVariant == null){
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Mobile Variant").build();
        }

        List<MobileVariantInstanceImpl> mobileVariantInstance =  findInstanceById(mobileVariant.getInstances(), instanceId);

        if(mobileVariantInstance.size() == 0){
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find requested Mobile Variant Instance").build();
        }

        // (currently) there is only one:
        MobileVariantInstanceImpl installationToDelete = mobileVariantInstance.get(0);

        // remove
        mobileVariant.getInstances().remove(installationToDelete);
        mobileApplicationInstanceService.removeMobileVariantInstances(mobileVariantInstance);

        return Response.noContent().build();

    }

    private List<MobileVariantInstanceImpl> findInstanceById(
            Set<MobileVariantInstanceImpl> instances, String instanceId) {
        final List<MobileVariantInstanceImpl> instancesWithToken = new ArrayList<MobileVariantInstanceImpl>();

        for (MobileVariantInstanceImpl instance : instances) {
            if (instance.getId().equals(instanceId))
                instancesWithToken.add(instance);
        }

        return instancesWithToken;
    }

    private MobileVariantInstanceImpl updateMobileApplicationInstance(
            MobileVariantInstanceImpl toUpdate,
            MobileVariantInstanceImpl postedVariant) {
        toUpdate.setCategory(postedVariant.getCategory());
        toUpdate.setDeviceToken(postedVariant.getDeviceToken());
        toUpdate.setAlias(postedVariant.getAlias());
        toUpdate.setDeviceType(postedVariant.getDeviceType());
        toUpdate.setMobileOperatingSystem(postedVariant
                .getMobileOperatingSystem());
        toUpdate.setOsVersion(postedVariant.getOsVersion());

        // update
        return mobileApplicationInstanceService
                .updateMobileVariantInstance(toUpdate);
    }




}


