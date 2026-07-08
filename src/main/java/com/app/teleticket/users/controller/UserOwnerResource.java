package com.app.teleticket.users.controller;

import com.app.teleticket.users.dto.UserCreateDTO;
import com.app.teleticket.users.dto.UserCreateForm;
import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.service.UserOwnerService;
import com.app.teleticket.users.utils.UserFormMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Everything related to the OWNER role: account creation (ADMIN only).
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserOwnerResource {

    @Inject
    UserOwnerService ownerService;

    @POST
    @Path("/owner")
    @RolesAllowed("ADMIN")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Create an OWNER account with optional profile picture")
    public Response createOwner(@BeanParam @Valid UserCreateForm form) {
        UserCreateDTO dto = UserFormMapper.toCreateDTO(form);
        UserResponseDTO created = ownerService.create(dto,
                UserFormMapper.photoBytes(form.getPhoto()),
                UserFormMapper.photoContentType(form.getPhoto()));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
