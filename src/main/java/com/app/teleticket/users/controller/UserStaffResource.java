package com.app.teleticket.users.controller;

import com.app.teleticket.common.dto.ApiResponse;
import com.app.teleticket.users.dto.DisaffiliateStaffEventRequest;
import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.dto.UserStaffCreateDTO;
import com.app.teleticket.users.dto.UserStaffCreateForm;
import com.app.teleticket.users.service.UserStaffService;
import com.app.teleticket.users.utils.UserFormMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Everything related to the STAFF role: creation + event affiliation, and disaffiliate.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserStaffResource {

    @Inject
    UserStaffService staffService;

    @POST
    @Path("/staff")
    @RolesAllowed({"OWNER", "ADMIN"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Create a STAFF account, affiliate it to an event, optional profile picture")
    public Response createStaff(@BeanParam @Valid UserStaffCreateForm form) {
        UserStaffCreateDTO dto = UserFormMapper.toStaffCreateDTO(form);
        UserResponseDTO created = staffService.create(dto,
                UserFormMapper.photoBytes(form.getPhoto()),
                UserFormMapper.photoContentType(form.getPhoto()));
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created(created))
                .build();
    }

    @DELETE
    @Path("/staff/disaffiliate")
    @RolesAllowed({"OWNER", "ADMIN"})
    @Operation(summary = "Disaffiliate a staff user from an event")
    public ApiResponse<UserResponseDTO> disaffiliateStaff(@Valid DisaffiliateStaffEventRequest request) {
        staffService.desaffiliate(request.getUserId(), request.getEventId());
        return ApiResponse.ok(null);
    }
}
