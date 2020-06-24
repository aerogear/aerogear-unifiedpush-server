package org.jboss.aerogear.unifiedpush.rest.util.error;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    public JsonProcessingExceptionMapper() {
    }

    @Override
    public Response toResponse(JsonProcessingException exception) {
        return Response.serverError().entity(ErrorBuilder.forServer().generalException(exception).build()).build();
    }
}