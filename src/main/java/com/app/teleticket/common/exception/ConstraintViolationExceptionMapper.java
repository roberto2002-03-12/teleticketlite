package com.app.teleticket.common.exception;

import com.app.teleticket.common.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.stream.Collectors;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .collect(Collectors.joining(", "));
        return Response.status(400)
                .entity(ApiResponse.error(400, message, null))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}