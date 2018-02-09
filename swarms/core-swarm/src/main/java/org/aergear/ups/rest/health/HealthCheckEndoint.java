package org.aergear.ups.rest.health;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Path("/sys/info")
public class HealthCheckEndoint {

    /**
     * Simple Ping endpoint to check if the UPS is running as expected
     *
     * @return simple OK string if the server is running
     */
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        return Response.ok("OK").build();
    }

    /**
     * Endpoint to verify scm and version details
     *
     * @return {@link Attributes}
     * @statuscode 200 Successful response for your request with manifest's attributes
     * @statuscode 404 Not found version information
     */
    @GET
    @Path("/version")
    @Produces(MediaType.APPLICATION_JSON)
    public Response manifestDetails(@Context HttpServletRequest request) {

        final ServletContext context = request.getSession().getServletContext();

        try (final InputStream manifestStream = context.getResourceAsStream("/META-INF/MANIFEST.MF")) {
            return Response.ok(new Manifest(manifestStream).getMainAttributes()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).entity("Could not find version information").build();
        }
    }
}
