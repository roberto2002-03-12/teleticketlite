package com.app.teleticket.users.controller;

import com.app.teleticket.common.dto.ApiResponse;
import com.app.teleticket.users.service.UserAdminService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

/**
 * Todo lo relacionado con el rol ADMIN: eliminación de cuentas.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
public class UserAdminResource {

    @Inject
    UserAdminService adminService;

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    @Operation(summary = "Delete an account by id")
    public ApiResponse<Void> deleteAccount(@PathParam("id") Long id) {
        adminService.deleteAccount(id);
        return ApiResponse.ok(null);
    }
}
