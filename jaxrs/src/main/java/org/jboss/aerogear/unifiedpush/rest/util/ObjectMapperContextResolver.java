package org.jboss.aerogear.unifiedpush.rest.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

import javax.ws.rs.ext.ContextResolver;

public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {
    private ObjectMapper objectMapper;

    public ObjectMapper getContext(Class<?> objectType) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.setAnnotationIntrospector(new JaxbAnnotationIntrospector());
        }
        return objectMapper;
    }
}
