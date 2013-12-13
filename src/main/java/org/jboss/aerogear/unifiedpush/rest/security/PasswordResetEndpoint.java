package org.jboss.aerogear.unifiedpush.rest.security;

import org.jboss.aerogear.security.authz.IdentityManagement;
import org.jboss.aerogear.security.token.service.TokenService;
import org.jboss.aerogear.unifiedpush.model.token.Credential;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.User;
import org.picketlink.idm.credential.Password;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

@Stateless
@Path("/")
public class PasswordResetEndpoint {

    @Inject
    private PartitionManager partitionManager;

    @Inject
    private TokenService tokenService;

    private IdentityManager identityManager;
    private RelationshipManager relationshipManager;

    @Inject
    private IdentityManagement<User> configuration;

    private static final Logger LOGGER = Logger.getLogger(PasswordResetEndpoint.class.getSimpleName());

    @GET
    @Path("/forgot")
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgot(@QueryParam("email") String email) {
        tokenService.send(email);
        return Response.status(NO_CONTENT).build();
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reset(Credential credential) {
        if (tokenService.isValid(credential.getToken())) {
            this.identityManager = partitionManager.createIdentityManager();
            tokenService.destroy(credential.getToken());
            User simpleUser = configuration.findByUsername(credential.getEmail());
            Password password = new Password(credential.getPassword().toCharArray());
            identityManager.updateCredential(simpleUser, password);
            return Response.status(NO_CONTENT)
                    .type(MediaType.TEXT_PLAIN)
                    .entity(NO_CONTENT).build();
        } else {
            return Response.status(NOT_FOUND).build();
        }
    }
}
