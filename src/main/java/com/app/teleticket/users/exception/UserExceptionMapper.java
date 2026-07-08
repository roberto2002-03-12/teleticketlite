package com.app.teleticket.users.exception;

import com.app.teleticket.common.dto.ApiResponse;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UserExceptionMapper implements ExceptionMapper<UserException> {

    @Override
    public Response toResponse(UserException exception) {
        return Response.status(exception.getStatus())
                .entity(ApiResponse.error(exception.getStatus(), exception.getMessage(), null))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
