package com.app.teleticket.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.FormParam;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.time.LocalDate;

public class UserStaffCreateForm {

    @FormParam("email")
    @NotBlank
    @Email
    @Size(max = 105)
    public String email;

    @FormParam("password")
    @NotBlank
    @Size(max = 32, min = 8)
    public String password;

    @FormParam("phoneNumber")
    @NotBlank
    @Size(max = 15)
    @Pattern(regexp = "^\\+?[0-9]{6,15}$")
    public String phoneNumber;

    @FormParam("fullname")
    @NotBlank
    @Size(max = 105)
    public String fullname;

    @FormParam("birthdate")
    @NotNull
    @Past
    public LocalDate birthdate;

    @FormParam("dni")
    @NotBlank
    @Pattern(regexp = "^[0-9]{8}$")
    public String dni;

    @FormParam("eventId")
    @NotNull
    @Positive
    public Integer eventId;

    @FormParam("photo")
    public FileUpload photo;
}
