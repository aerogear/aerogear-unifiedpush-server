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

package org.jboss.aerogear.connectivity.cdi.interceptor;

import org.jboss.aerogear.security.authz.IdentityManagement;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.HashSet;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SecurityInterceptorTest {

    @Mock
    private InvocationContext invocationContext;

    @Mock
    private IdentityManagement identityManagement;

    @InjectMocks
    private SecurityInterceptor securityInterceptor;

    private Method method;

    private MobileAppEndpoint mobileAppEndpoint;

    private HashSet<String> roles;

    @Before
    public void setUp() throws Exception {
        securityInterceptor = new SecurityInterceptor();
        MockitoAnnotations.initMocks(this);
        mobileAppEndpoint = new MobileAppEndpoint();
        roles = new HashSet<String>();
    }

    @Test
    public void testAuthorizedRequestProtectedResource() throws Exception {
        roles.add("admin");
        method = mobileAppEndpoint.getClass().getMethod("registerMobileDevice");
        when(invocationContext.getMethod()).thenReturn(method);
        when(identityManagement.hasRoles(roles)).thenReturn(true);
        securityInterceptor.invoke(invocationContext);
        verify(invocationContext).proceed();
    }

    @Test(expected = RuntimeException.class)
    public void testNonAuthorizedRequestProtectedResource() throws Exception {
        roles.add("simple");
        method = mobileAppEndpoint.getClass().getMethod("registerMobileDevice");
        when(invocationContext.getMethod()).thenReturn(method);
        when(identityManagement.hasRoles(roles)).thenReturn(false);
        securityInterceptor.invoke(invocationContext);
    }

    @Test
    public void testRequestNonProtectedResource() throws Exception {
        method = mobileAppEndpoint.getClass().getMethod("retrieveMobileDevices");
        when(invocationContext.getMethod()).thenReturn(method);
        Object result = securityInterceptor.invoke(invocationContext);
        verify(invocationContext).proceed();
    }

    //Stub
    private class MobileAppEndpoint {

        @Secure("admin")
        public void registerMobileDevice() {}

        public void retrieveMobileDevices() {}
    }
}
