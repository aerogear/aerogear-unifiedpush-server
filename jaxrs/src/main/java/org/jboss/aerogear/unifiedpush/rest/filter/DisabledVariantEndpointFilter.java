/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.jboss.aerogear.unifiedpush.rest.annotations.DisabledByEnvironment;
import org.jboss.aerogear.unifiedpush.system.ConfigurationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter will return a 404 if the requested path matches a
 * DisabledByEnvironment annotation
 */
@Provider
@DisabledByEnvironment
public class DisabledVariantEndpointFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DisabledVariantEndpointFilter.class.getName());

    private Set<String> globallyDisabledEndpoints = new HashSet<>();

    public DisabledVariantEndpointFilter() throws ServletException {
        String environmentString = ConfigurationUtils.tryGetGlobalProperty("UPS_DISABLED", "");

        String[] disabledEndpoints = (environmentString.split(","));

        Arrays.stream(disabledEndpoints).filter(env -> !env.isBlank()).forEach(globallyDisabledEndpoints::add);

        log.info(String.format("The following variant endpoints are disabled " + environmentString));

    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        List<Object> resources = requestContext.getUriInfo().getMatchedResources();

        for (Object resource : resources) {
            DisabledByEnvironment annotation = getAnnotation(resource.getClass());
            
            if (annotation != null) {
                for (String value : annotation.value()) {
                    if (globallyDisabledEndpoints.contains(value)) {
                        requestContext.abortWith(Response.status(404).build());
                        return;
                    }

                }
            }
        };
        
    }

    public static DisabledByEnvironment getAnnotation(Class<?> clazz) {
        while (clazz != null) {
            if (clazz.isAnnotationPresent(DisabledByEnvironment.class)) {
                return clazz.getAnnotation(DisabledByEnvironment.class);
            }
            clazz = clazz.getSuperclass();
        }

        return null;
    }

}
