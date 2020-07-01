package org.jboss.aerogear.unifiedpush.rest.util.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);

    public JsonProcessingExceptionMapper() {
        LOG.debug("Starting up");
    }

    @Override
    public Response toResponse(JsonProcessingException exception) {
        LOG.debug("Caught exception " + exception.getMessage());
        return Response.serverError().entity(ErrorBuilder.forServer().generalException(exception).build()).build();
    }
}