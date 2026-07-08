package com.app.teleticket.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "user")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(nullable = false, unique = true, length = 105)
    public String email;

    @Column(nullable = false, unique = true, length = 305)
    public String password;

    @Column(name = "phone_number", nullable = false, length = 15)
    public String phoneNumber;

    @Column(nullable = false, length = 105)
    public String fullname;

    @Column(name = "birthdate", nullable = false)
    public LocalDate birthdate;

    @Column(nullable = false, unique = true, length = 8)
    public String dni;

    @Column(name = "photo_url", length = 255)
    public String photoUrl;

    @Column(name = "photo_key_name", length = 105)
    public String photoKeyName;

    @Column(nullable = false, length = 45)
    public String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhotoKeyName() {
        return photoKeyName;
    }

    public void setPhotoKeyName(String photoKeyName) {
        this.photoKeyName = photoKeyName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
