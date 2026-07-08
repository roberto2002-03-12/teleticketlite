package com.app.teleticket.auth.service.impl;

import com.app.teleticket.auth.exception.AuthException;
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
        String email = readClaim("email");
        if (email != null) {
            return email;
        }
        String username = readClaim("username");
        if (username != null) {
            return username;
        }
        String cognitoUsername = readClaim("cognito:username");
        if (cognitoUsername != null) {
            return cognitoUsername;
        }
        String preferredUsername = readClaim("preferred_username");
        if (preferredUsername != null) {
            return preferredUsername;
        }
        throw new AuthException(401, "Authenticated token has no email/username claim");
    }

    private String readClaim(String name) {
        try {
            String value = jwt.getClaim(name);
            if (value != null && !value.isBlank()) {
                return value;
            }
        } catch (RuntimeException ignored) {
            // claim not present or unreadable
        }
        return null;
    }
}
