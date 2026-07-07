package com.app.teleticket.users.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "user")
public class UserEntity extends PanacheEntity {

    @Column(nullable = false, unique = true, length = 105)
    public String email;

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
}
