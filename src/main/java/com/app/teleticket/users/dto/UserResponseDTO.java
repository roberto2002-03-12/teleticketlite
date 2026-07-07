package com.app.teleticket.users.dto;

import java.time.LocalDate;

public record UserResponseDTO(
        Long id,
        String email,
        String phoneNumber,
        String fullname,
        LocalDate birthdate,
        String dni,
        String photoUrl,
        String role
) {
}
