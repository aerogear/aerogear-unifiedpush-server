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
