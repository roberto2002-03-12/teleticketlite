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
    private String fullname;

    @NotBlank
    @Size(max = 15)
    @Pattern(regexp = "^\\+?[0-9]{6,15}$")
    private String phoneNumber;

    @NotNull
    @Past
    private LocalDate birthdate;

    @NotBlank
    @Pattern(regexp = "^[0-9]{8}$")
    private String dni;

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }
}
