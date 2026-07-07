package com.app.teleticket.auth.controller;

import com.app.teleticket.auth.dto.LoginRequest;
import com.app.teleticket.auth.dto.LoginResponse;
import com.app.teleticket.auth.service.AuthLoginService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    AuthLoginService loginService;

    @POST
    @Path("/login")
    @Operation(summary = "Login with email + password against AWS Cognito (USER_PASSWORD_AUTH)")
    public LoginResponse login(@Valid LoginRequest request) {
        return loginService.login(request);
    }
}
