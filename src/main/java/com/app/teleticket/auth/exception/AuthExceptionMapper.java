package com.app.teleticket.auth.exception;

import com.app.teleticket.common.dto.ApiResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthExceptionMapper implements ExceptionMapper<AuthException> {

    @Override
    public Response toResponse(AuthException exception) {
        return Response.status(exception.getStatus())
                .entity(ApiResponse.error(exception.getStatus(), exception.getMessage(), null))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
