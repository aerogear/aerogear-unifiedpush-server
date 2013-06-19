package org.jboss.aerogear.connectivity.cdi.exception;


import static org.jboss.aerogear.security.exception.HttpStatus.AUTHENTICATION_FAILED;
import org.jboss.resteasy.spi.UnauthorizedException;

import javax.ejb.EJBException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;


@Provider
public class HttpExceptionMapper implements ExceptionMapper<EJBException> {

    @Override
    public Response toResponse(EJBException exception) {

        Exception e = exception.getCausedByException();

        if (e instanceof UnauthorizedException) {
            return Response.status(AUTHENTICATION_FAILED.getCode())
                    .entity(AUTHENTICATION_FAILED.toString())
                    .build();
        } else {
            return Response.status(BAD_REQUEST).build();
        }
    }
}

