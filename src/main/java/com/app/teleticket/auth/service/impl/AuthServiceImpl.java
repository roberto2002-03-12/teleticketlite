package com.app.teleticket.auth.service.impl;

import com.app.teleticket.auth.service.AuthService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class AuthServiceImpl implements AuthService {

    @Inject
    JsonWebToken jwt;

    @Override
    public String currentEmail() {
        return jwt.getClaim("email");
    }
}
