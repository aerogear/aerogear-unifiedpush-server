package org.jboss.aerogear.unifiedpush.rest.util.error;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class UnrecognizedPropertyExceptionHandler implements ExceptionMapper<UnrecognizedPropertyException> {
    public UnrecognizedPropertyExceptionHandler() {
    }

    public Response toResponse(UnrecognizedPropertyException exception) {
        return Response.serverError().entity(ErrorBuilder.forServer().generalException(exception).build()).build();
    }
}
