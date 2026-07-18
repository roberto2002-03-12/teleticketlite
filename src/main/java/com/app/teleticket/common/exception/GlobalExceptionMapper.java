package com.app.teleticket.common.exception;

import com.app.teleticket.common.dto.ApiResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * Mapper de último recurso para cualquier excepción no manejada. Produce el envoltorio
 * de error estándar {@link ApiResponse} y nunca filtra trazas de pila a los clientes.
 * {@code WebApplicationException} y los mappers de módulos existentes tienen prioridad.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof WebApplicationException wae) {
            int code = wae.getResponse() != null ? wae.getResponse().getStatus() : 500;
            String message = wae.getMessage() != null ? wae.getMessage() : "Web application error";
            return Response.status(code)
                    .entity(ApiResponse.error(code, message, null))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
        LOG.error("Unhandled exception", exception);
        return Response.serverError()
                .entity(ApiResponse.error(500, "Internal server error", null))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}