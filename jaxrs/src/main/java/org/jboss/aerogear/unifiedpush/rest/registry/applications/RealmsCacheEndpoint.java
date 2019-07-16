package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.aerogear.unifiedpush.service.RealmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
@Path("/realms")
public class RealmsCacheEndpoint {
    private final Logger logger = LoggerFactory.getLogger(RealmsCacheEndpoint.class);

    @Inject
    private RealmsService realmsService;

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearRealmsCache(){
        try {
            realmsService.clearCache();
            return Response.ok().build();
        } catch (Exception ex) {
            logger.error("Cannot clear realms cache. {}", ex.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ex.getMessage()).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(){
        return Response.ok(realmsService.getAll()).build();
    }

    @PUT
    @Path("/applicationName/{appName}/realmName/{realmName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response insert(@PathParam("appName") String appName, @PathParam("realmName") String realmName){
        realmsService.insert(appName, realmName);
        return Response.ok().build();
    }

}
