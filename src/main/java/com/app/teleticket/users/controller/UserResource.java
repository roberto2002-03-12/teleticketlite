package com.app.teleticket.users.controller;

import com.app.teleticket.auth.service.AuthService;
import com.app.teleticket.users.dto.UserCreateDTO;
import com.app.teleticket.users.dto.UserCreateForm;
import com.app.teleticket.users.dto.UserPhotoForm;
import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.dto.UserStaffCreateDTO;
import com.app.teleticket.users.dto.UserStaffCreateForm;
import com.app.teleticket.users.dto.UserUpdateDTO;
import com.app.teleticket.users.service.UserAdminService;
import com.app.teleticket.users.service.UserClientService;
import com.app.teleticket.users.service.UserOwnerService;
import com.app.teleticket.users.service.UserProfileService;
import com.app.teleticket.users.service.UserStaffService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserClientService ClientService;

    @Inject
    UserStaffService staffService;

    @Inject
    UserOwnerService ownerService;

    @Inject
    UserAdminService adminService;

    @Inject
    UserProfileService ProfileService;

    @Inject
    AuthService auth;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Register a new CLIENT account (DB + AWS Cognito) with optional profile picture")
    public Response createClient(@MultipartForm @Valid UserCreateForm form) {
        UserCreateDTO dto = toCreateDTO(form);
        UserResponseDTO created = ClientService.create(dto, photoBytes(form.photo), photoContentType(form.photo));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/staff")
    @RolesAllowed("OWNER")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Create a STAFF account, affiliate it to an event, optional profile picture")
    public Response createStaff(@MultipartForm @Valid UserStaffCreateForm form) {
        UserStaffCreateDTO dto = toStaffCreateDTO(form);
        UserResponseDTO created = staffService.create(dto, photoBytes(form.photo), photoContentType(form.photo));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/owner")
    @RolesAllowed("ADMIN")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Create an OWNER account with optional profile picture")
    public Response createOwner(@MultipartForm @Valid UserCreateForm form) {
        UserCreateDTO dto = toCreateDTO(form);
        UserResponseDTO created = ownerService.create(dto, photoBytes(form.photo), photoContentType(form.photo));
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/me")
    @RolesAllowed({"CLIENT", "STAFF", "OWNER", "ADMIN"})
    @Operation(summary = "Get the authenticated user's own profile")
    public UserResponseDTO getMe() {
        return ProfileService.getMe(auth.currentEmail());
    }

    @PUT
    @Path("/me")
    @RolesAllowed({"CLIENT", "STAFF", "OWNER", "ADMIN"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "Edit the authenticated user's own profile")
    public UserResponseDTO updateMe(@Valid UserUpdateDTO dto) {
        return ProfileService.updateMe(auth.currentEmail(), dto);
    }

    @POST
    @Path("/me/photo")
    @RolesAllowed({"CLIENT", "STAFF", "OWNER", "ADMIN"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "Upload or replace the authenticated user's profile picture")
    public UserResponseDTO uploadPhoto(@MultipartForm @Valid UserPhotoForm form) {
        return ProfileService.uploadPhoto(auth.currentEmail(), photoBytes(form.photo), photoContentType(form.photo));
    }

    @DELETE
    @Path("/me/photo")
    @RolesAllowed({"CLIENT", "STAFF", "OWNER", "ADMIN"})
    @Operation(summary = "Delete the authenticated user's profile picture")
    public Response deletePhoto() {
        ProfileService.deletePhoto(auth.currentEmail());
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Delete an account by id")
    public Response deleteAccount(@PathParam("id") Long id) {
        adminService.deleteAccount(id);
        return Response.noContent().build();
    }

    @DELETE
    @Path("/{userId}/staff/{eventId}")
    @RolesAllowed({"OWNER", "ADMIN"})
    @Operation(summary = "Desaffiliate a staff user from an event")
    public Response desaffiliateStaff(@PathParam("userId") Integer userId,
                                      @PathParam("eventId") Integer eventId) {
        staffService.desaffiliate(userId, eventId);
        return Response.noContent().build();
    }

    private UserCreateDTO toCreateDTO(UserCreateForm form) {
        UserCreateDTO dto = new UserCreateDTO();
        dto.email = form.email;
        dto.phoneNumber = form.phoneNumber;
        dto.fullname = form.fullname;
        dto.birthdate = form.birthdate;
        dto.dni = form.dni;
        return dto;
    }

    private UserStaffCreateDTO toStaffCreateDTO(UserStaffCreateForm form) {
        UserStaffCreateDTO dto = new UserStaffCreateDTO();
        dto.email = form.email;
        dto.phoneNumber = form.phoneNumber;
        dto.fullname = form.fullname;
        dto.birthdate = form.birthdate;
        dto.dni = form.dni;
        dto.eventId = form.eventId;
        return dto;
    }

    private byte[] photoBytes(FileUpload photo) {
        if (photo == null) {
            return null;
        }
        try {
            return Files.readAllBytes(photo.uploadedFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String photoContentType(FileUpload photo) {
        return photo == null ? null : photo.contentType();
    }
}
