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
import org.jboss.resteasy.spi.UnauthorizedException;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Interceptor
@Secure({})
public class SecurityInterceptor {

    private static final Logger LOGGER = Logger.getLogger(SecurityInterceptor.class.getSimpleName());

    @Inject
    private IdentityManagement<?> identityManagement;

    @AroundInvoke
    public Object invoke(InvocationContext ctx) throws Exception {

        Class clazz = ctx.getTarget().getClass();

        if (clazz.isAnnotationPresent(Secure.class)) {
            authorize(clazzMetadata(ctx));
        }

        Method method = ctx.getMethod();

        if (method.isAnnotationPresent(Secure.class)) {
            authorize(methodMetadata(ctx));
        }

        return ctx.proceed();
    }

    private Set<String> clazzMetadata(InvocationContext ctx) {
        Secure annotation = ctx.getTarget().getClass().getAnnotation(Secure.class);
        return new HashSet<String>(Arrays.asList(annotation.value()));
    }

    private Set<String> methodMetadata(InvocationContext ctx) {
        Secure annotation = ctx.getMethod().getAnnotation(Secure.class);
        return new HashSet<String>(Arrays.asList(annotation.value()));
    }

    private void authorize(Set<String> roles) {
        boolean hasRoles = identityManagement.hasRoles(roles);

        if (!hasRoles)
            throw new UnauthorizedException("Not authorized!");
    }
}
