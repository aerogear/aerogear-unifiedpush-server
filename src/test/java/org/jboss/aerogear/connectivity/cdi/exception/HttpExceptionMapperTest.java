package org.jboss.aerogear.connectivity.cdi.exception;

import org.jboss.resteasy.spi.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ejb.EJBException;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class HttpExceptionMapperTest {

    @Mock
    private EJBException ejbException;

    @InjectMocks
    private HttpExceptionMapper exceptionMapper = new HttpExceptionMapper();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUnauthorizedResponse() throws Exception {
        when(ejbException.getCausedByException()).thenReturn(new UnauthorizedException("Not authorized here"));
        Response response = exceptionMapper.toResponse(ejbException);
        assertEquals(response.getStatus(), UNAUTHORIZED.getStatusCode());
    }

    @Test
    public void testBadRequestResponse() throws Exception {
        when(ejbException.getCausedByException()).thenReturn(new Exception("Any other exception"));
        Response response = exceptionMapper.toResponse(ejbException);
        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
    }
}
