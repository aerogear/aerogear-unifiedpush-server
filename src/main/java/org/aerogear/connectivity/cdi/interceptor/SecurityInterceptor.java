package org.aerogear.connectivity.cdi.interceptor;


import org.jboss.aerogear.security.authz.IdentityManagement;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Interceptor
@Secure({})
public class SecurityInterceptor {

    @Inject
    private IdentityManagement identityManagement;

    @AroundInvoke
    public Object invoke(InvocationContext ctx) throws Exception {

        Method method = ctx.getMethod();

        if (method.isAnnotationPresent(Secure.class)) {

            Secure annotation = ctx.getMethod().getAnnotation(Secure.class);
            Set<String> roles = new HashSet<String>(Arrays.asList(annotation.value()));
            boolean hasRoles = identityManagement.hasRoles(roles);

            if (!hasRoles)
                throw new RuntimeException("Not authorized!");

        }

        return ctx.proceed();
    }
}
