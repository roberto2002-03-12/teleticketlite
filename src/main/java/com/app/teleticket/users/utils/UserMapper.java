package com.app.teleticket.users.utils;

import com.app.teleticket.users.dto.UserCreateDTO;
import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.entity.UserEntity;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper {

    public UserEntity toEntity(UserCreateDTO dto) {
        UserEntity entity = new UserEntity();
        entity.email = dto.email;
        entity.phoneNumber = dto.phoneNumber;
        entity.fullname = dto.fullname;
        entity.birthdate = dto.birthdate;
        entity.dni = dto.dni;
        entity.photoUrl = null;
        entity.photoKeyName = null;
        return entity;
    }

    public UserResponseDTO toResponse(UserEntity entity) {
        return new UserResponseDTO(
                entity.id,
                entity.email,
                entity.phoneNumber,
                entity.fullname,
                entity.birthdate,
                entity.dni,
                entity.photoUrl,
                entity.role
        );
    }
}
