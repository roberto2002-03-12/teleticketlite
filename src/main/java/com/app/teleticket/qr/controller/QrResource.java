package com.app.teleticket.qr.controller;

import com.app.teleticket.auth.service.AuthService;
import com.app.teleticket.common.dto.ApiResponse;
import com.app.teleticket.qr.dto.*;
import com.app.teleticket.qr.service.EventAssistantService;
import com.app.teleticket.qr.service.ListClientsEventsRegistered;
import com.app.teleticket.qr.service.QrRegisterService;
import com.app.teleticket.qr.service.QrValidationService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;

@Path("/qr")
@Produces(MediaType.APPLICATION_JSON)
public class QrResource {

    @Inject
    AuthService auth;

    @Inject
    QrRegisterService registerService;

    @Inject
    QrValidationService validationService;

    @Inject
    EventAssistantService assistantService;

    @Inject
    ListClientsEventsRegistered listClientsEventsRegistered;

    @POST
    @Path("/events/{eventId}/tickets")
    @RolesAllowed("CLIENT")
    @Operation(summary = "Register the current CLIENT user for an event and generate a QR ticket")
    public Response register(@PathParam("eventId") Integer eventId) {
        QrTicketResponseDTO created = registerService.register(auth.currentEmail(), eventId);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created(created))
                .build();
    }

    @POST
    @Path("/tickets/validate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Validate a QR ticket image and mark it as applied if valid (public)")
    public ApiResponse<QrValidationResponseDTO> validate(@BeanParam @Valid QrScanForm form) {
        return ApiResponse.ok(validationService.validate(fileBytes(form), contentType(form)));
    }

    @GET
    @Path("/events/{eventId}/assistants")
    @RolesAllowed("OWNER")
    @Operation(summary = "List assistants registered to an event owned by the current user")
    public ApiResponse<List<EventAssistantResponseDTO>> listAssistants(@PathParam("eventId") Integer eventId) {
        return ApiResponse.ok(assistantService.listAssistants(auth.currentEmail(), eventId));
    }

    @GET
    @Path("/me/events")
    @RolesAllowed("CLIENT")
    @Operation(summary = "Lista los eventos en los cuales el usuario se a registrado")
    public ApiResponse<List<MyInscriptionsDTO>> listMyInscriptions() {
        return ApiResponse.ok(listClientsEventsRegistered.getMyInscriptions(auth.currentEmail()));
    }

    private byte[] fileBytes(QrScanForm form) {
        if (form.getQr() == null) {
            return new byte[0];
        }
        try {
            return Files.readAllBytes(form.getQr().uploadedFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String contentType(QrScanForm form) {
        return form.getQr() == null ? null : form.getQr().contentType();
    }
}