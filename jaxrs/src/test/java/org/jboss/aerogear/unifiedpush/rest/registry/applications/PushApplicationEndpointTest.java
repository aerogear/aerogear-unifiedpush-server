package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import org.jboss.aerogear.unifiedpush.api.PushApplication;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Created on 24/08/17.
 */
public class PushApplicationEndpointTest {

    private final PushApplicationEndpoint endpoint = new PushApplicationEndpoint();

    @Test
    public void shouldReturn409IfApplicationExists() throws Exception {
        // Given
        PushApplication pa = new PushApplication();
        pa.setName("EJB Container");
        final String uuid = UUID.randomUUID().toString();
        pa.setPushApplicationID(uuid);

        PushApplication pa2 = new PushApplication();
        pa2.setName("EJB Container 2");
        pa2.setPushApplicationID(uuid);

        endpoint.registerPushApplication(pa);

        // Then
        Response res = endpoint.registerPushApplication(pa2);
        assertEquals(res.getStatus(), Response.Status.CONFLICT.getStatusCode());
    }
}