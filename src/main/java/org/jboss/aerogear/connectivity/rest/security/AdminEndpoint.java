package org.jboss.aerogear.connectivity.rest.security;

import org.jboss.aerogear.connectivity.users.Developer;
import org.jboss.aerogear.security.authz.IdentityManagement;
import org.jboss.aerogear.security.authz.Secure;
import org.picketlink.Identity;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Stateless
@Path("/admin")
public class AdminEndpoint {

    @Inject
    private IdentityManagement configuration;
    @Inject
    private IdentityManager identityManager;

    @Inject
    private Identity identity;

    @POST
    @Path("/enroll")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Secure("admin")
    public Response enroll(final Developer developer) {
        try {
            configuration.create(developer, developer.getPassword());
            configuration.grant(developer.getRole()).to(developer.getLoginName());

        } catch (IdentityManagementException ime) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Credential not available").build();
        }

        return Response.ok(developer).build();

    }
}
