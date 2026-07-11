package com.app.teleticket.events.controller;

import com.app.teleticket.common.dto.ApiResponse;
import com.app.teleticket.events.dto.EventCategoryCreateDTO;
import com.app.teleticket.events.dto.EventCategoryResponseDTO;
import com.app.teleticket.events.service.EventCategoryService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

@Path("/events/categories")
@Produces(MediaType.APPLICATION_JSON)
public class EventCategoryResource {

    @Inject
    EventCategoryService categoryService;

    @POST
    @RolesAllowed("ADMIN")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Create an event category")
    public Response create(@Valid EventCategoryCreateDTO dto) {
        EventCategoryResponseDTO created = categoryService.create(dto);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created(created))
                .build();
    }

    @GET
    @RolesAllowed({"CLIENT", "STAFF", "OWNER", "ADMIN"})
    @Operation(summary = "List all event categories")
    public ApiResponse<List<EventCategoryResponseDTO>> list() {
        return ApiResponse.ok(categoryService.list());
    }
}