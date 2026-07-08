package com.app.teleticket.users.controller;

import com.app.teleticket.auth.service.AuthService;
import com.app.teleticket.common.dto.ApiResponse;
import com.app.teleticket.users.dto.UserCreateDTO;
import com.app.teleticket.users.dto.UserCreateForm;
import com.app.teleticket.users.dto.UserPhotoForm;
import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.dto.UserUpdateDTO;
import com.app.teleticket.users.service.UserClientService;
import com.app.teleticket.users.service.UserProfileService;
import com.app.teleticket.users.utils.UserFormMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Everything related to the CLIENT role: public self-registration, plus the
 * shared "me" self-service endpoints available to any authenticated user.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserClientResource {

    @Inject
    UserClientService clientService;

    @Inject
    UserProfileService profileService;

    @Inject
    AuthService auth;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Register a new CLIENT account (DB + AWS Cognito) with optional profile picture")
    public Response createClient(@BeanParam @Valid UserCreateForm form) {
        UserCreateDTO dto = UserFormMapper.toCreateDTO(form);
        UserResponseDTO created = clientService.create(dto,
                UserFormMapper.photoBytes(form.getPhoto()),
                UserFormMapper.photoContentType(form.getPhoto()));
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.created(created))
                .build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"CLIENT", "STAFF", "OWNER", "ADMIN"})
    @Operation(summary = "Get the authenticated user's own profile")
    public ApiResponse<UserResponseDTO> getMe() {
        return ApiResponse.ok(profileService.getMe(auth.currentEmail()));
    }

    @PUT
    @Path("/me")
    @RolesAllowed({"CLIENT", "STAFF", "OWNER", "ADMIN"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Edit the authenticated user's own profile")
    public ApiResponse<UserResponseDTO> updateMe(@Valid UserUpdateDTO dto) {
        return ApiResponse.ok(profileService.updateMe(auth.currentEmail(), dto));
    }

    @POST
    @Path("/me/photo")
    @RolesAllowed({"CLIENT", "STAFF", "OWNER", "ADMIN"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Upload or replace the authenticated user's profile picture")
    public ApiResponse<UserResponseDTO> uploadPhoto(@BeanParam @Valid UserPhotoForm form) {
        return ApiResponse.ok(profileService.uploadPhoto(auth.currentEmail(),
                UserFormMapper.photoBytes(form.getPhoto()),
                UserFormMapper.photoContentType(form.getPhoto())));
    }

    @DELETE
    @Path("/me/photo")
    @RolesAllowed({"CLIENT", "STAFF", "OWNER", "ADMIN"})
    @Operation(summary = "Delete the authenticated user's profile picture")
    public ApiResponse<UserResponseDTO> deletePhoto() {
        profileService.deletePhoto(auth.currentEmail());
        return ApiResponse.ok(null);
    }
}
