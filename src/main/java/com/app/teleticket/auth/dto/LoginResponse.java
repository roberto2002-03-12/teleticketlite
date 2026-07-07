package com.app.teleticket.auth.dto;

public record LoginResponse(
        String accessToken,
        String idToken,
        String refreshToken,
        Integer expiresIn,
        String tokenType
) {
}
