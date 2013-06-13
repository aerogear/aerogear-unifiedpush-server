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

package org.aerogear.connectivity.cdi.interceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.jboss.aerogear.security.authz.IdentityManagement;
import org.jboss.resteasy.spi.UnauthorizedException;

@Interceptor
@Secure({})
public class SecurityInterceptor {

    @Inject
    private IdentityManagement<?> identityManagement;

    @AroundInvoke
    public Object invoke(InvocationContext ctx) throws Exception {

        Method method = ctx.getMethod();

        if (method.isAnnotationPresent(Secure.class)) {

            Secure annotation = ctx.getMethod().getAnnotation(Secure.class);
            Set<String> roles = new HashSet<String>(Arrays.asList(annotation.value()));
            System.out.println("\n\n\n\n\n   "   + roles);
            boolean hasRoles = identityManagement.hasRoles(roles);
            System.out.println("\n\n\n\n\n   "   + hasRoles);
            

            if (!hasRoles)
                throw new UnauthorizedException("Not authorized!");

        }

        return ctx.proceed();
    }
}
