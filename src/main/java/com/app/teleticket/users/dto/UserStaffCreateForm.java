package com.app.teleticket.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.time.LocalDate;

public class UserStaffCreateForm {

    @RestForm("email")
    @NotBlank
    @Email
    @Size(max = 105)
    private String email;

    @RestForm("password")
    @NotBlank
    @Size(max = 32, min = 8)
    private String password;

    @RestForm("phoneNumber")
    @NotBlank
    @Size(max = 15)
    @Pattern(regexp = "^\\+?[0-9]{6,15}$")
    private String phoneNumber;

    @RestForm("fullname")
    @NotBlank
    @Size(max = 105)
    private String fullname;

    @RestForm("birthdate")
    @NotNull
    @Past
    private LocalDate birthdate;

    @RestForm("dni")
    @NotBlank
    @Pattern(regexp = "^[0-9]{8}$")
    private String dni;

    @RestForm("eventId")
    @NotNull
    @Positive
    private Integer eventId;

    @RestForm("photo")
    private FileUpload photo;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
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

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public FileUpload getPhoto() {
        return photo;
    }

    public void setPhoto(FileUpload photo) {
        this.photo = photo;
    }
}
