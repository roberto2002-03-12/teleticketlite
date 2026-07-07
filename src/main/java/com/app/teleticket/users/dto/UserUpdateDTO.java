package com.app.teleticket.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UserUpdateDTO {

    @NotBlank
    @Size(max = 105)
    public String fullname;

    @NotBlank
    @Size(max = 15)
    @Pattern(regexp = "^\\+?[0-9]{6,15}$")
    public String phoneNumber;

    @NotNull
    @Past
    public LocalDate birthdate;

    @NotBlank
    @Pattern(regexp = "^[0-9]{8}$")
    public String dni;
}
