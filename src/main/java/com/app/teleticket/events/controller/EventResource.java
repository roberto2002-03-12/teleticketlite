package com.app.teleticket.events.controller;

import com.app.teleticket.auth.service.AuthService;
import com.app.teleticket.common.dto.ApiResponse;
import com.app.teleticket.events.dto.EventCreateDTO;
import com.app.teleticket.events.dto.EventCreateForm;
import com.app.teleticket.events.dto.EventImageInput;
import com.app.teleticket.events.dto.PageResponse;
import com.app.teleticket.events.dto.EventResponseDTO;
import com.app.teleticket.events.dto.EventStaffUpdateDTO;
import com.app.teleticket.events.dto.EventUpdateDTO;
import com.app.teleticket.events.service.EventClientService;
import com.app.teleticket.events.service.EventOwnerService;
import com.app.teleticket.events.service.EventStaffService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/events")
@Produces(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject
    EventOwnerService ownerService;

    @Inject
    EventStaffService staffService;

    @Inject
    EventClientService clientService;

    @Inject
    AuthService auth;

    @POST
    @RolesAllowed({"OWNER", "ADMIN"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Create an event with up to 8 images (jpg/jpeg/png)")
    public Response create(@BeanParam @Valid EventCreateForm form) {
        EventCreateDTO dto = toCreateDTO(form);
        EventResponseDTO created = ownerService.create(auth.currentEmail(), dto);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created(created))
                .build();
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed({"OWNER", "ADMIN"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update an event owned by the current user")
    public ApiResponse<EventResponseDTO> update(@PathParam("id") Integer id, @Valid EventUpdateDTO dto) {
        return ApiResponse.ok(ownerService.update(auth.currentEmail(), id, dto));
    }

    @PUT
    @Path("/{id}/cancel")
    @RolesAllowed({"OWNER", "ADMIN"})
    @Operation(summary = "Cancel an event owned by the current user (set available=false)")
    public ApiResponse<EventResponseDTO> cancel(@PathParam("id") Integer id) {
        return ApiResponse.ok(ownerService.cancel(auth.currentEmail(), id));
    }

    @GET
    @Path("/me")
    @RolesAllowed({"OWNER", "ADMIN"})
    @Operation(summary = "List events owned by the current user")
    public ApiResponse<List<EventResponseDTO>> listOwn() {
        return ApiResponse.ok(ownerService.listOwn(auth.currentEmail()));
    }

    @GET
    @Path("/me/staff")
    @RolesAllowed("STAFF")
    @Operation(summary = "List events the current staff user is affiliated with")
    public ApiResponse<List<EventResponseDTO>> listAffiliated() {
        return ApiResponse.ok(staffService.listAffiliated(auth.currentEmail()));
    }

    @PUT
    @Path("/{id}/staff")
    @RolesAllowed("STAFF")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Update description and category of an event the staff user is affiliated with")
    public ApiResponse<EventResponseDTO> updateStaffFields(@PathParam("id") Integer id, @Valid EventStaffUpdateDTO dto) {
        return ApiResponse.ok(staffService.updateStaffFields(auth.currentEmail(), id, dto));
    }

    @GET
    @RolesAllowed("CLIENT")
    @Operation(summary = "Search active events (available=true) with filters and pagination (12 per page)")
    public ApiResponse<PageResponse<EventResponseDTO>> search(@QueryParam("title") String title,
                                                              @QueryParam("startDate") LocalDateTime startDate,
                                                              @QueryParam("finishDate") LocalDateTime finishDate,
                                                              @QueryParam("categoryId") Integer categoryId,
                                                              @QueryParam("page") @DefaultValue("0") int page) {
        return ApiResponse.ok(clientService.search(title, startDate, finishDate, categoryId, page));
    }

    @GET
    @Path("/{id}")
    @RolesAllowed("CLIENT")
    @Operation(summary = "Select an active event by id")
    public ApiResponse<EventResponseDTO> getById(@PathParam("id") Integer id) {
        return ApiResponse.ok(clientService.getActiveById(id));
    }

    private EventCreateDTO toCreateDTO(EventCreateForm form) {
        EventCreateDTO dto = new EventCreateDTO();
        dto.setTitle(form.getTitle());
        dto.setDescription(form.getDescription());
        dto.setMaxPeople(form.getMaxPeople());
        dto.setAddress(form.getAddress());
        dto.setAvailable(form.isAvailable());
        dto.setFinished(form.isFinished());
        dto.setStartDate(form.getStartDate());
        dto.setFinishDate(form.getFinishDate());
        dto.setCategoryId(form.getCategoryId());
        dto.setPhotos(toImageInputs(form.getPhotos()));
        return dto;
    }

    private List<EventImageInput> toImageInputs(List<FileUpload> photos) {
        if (photos == null || photos.isEmpty()) {
            return List.of();
        }
        List<EventImageInput> inputs = new ArrayList<>(photos.size());
        for (FileUpload photo : photos) {
            byte[] bytes;
            try {
                bytes = Files.readAllBytes(photo.uploadedFile());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            inputs.add(new EventImageInput(bytes, photo.contentType()));
        }
        return inputs;
    }
}