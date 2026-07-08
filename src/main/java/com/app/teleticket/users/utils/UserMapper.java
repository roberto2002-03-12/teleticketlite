package com.app.teleticket.users.utils;

import com.app.teleticket.users.dto.UserCreateDTO;
import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.entity.UserEntity;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserMapper {

    public UserEntity toEntity(UserCreateDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setEmail(dto.getEmail());
        entity.setPassword(BcryptUtil.bcryptHash(dto.getPassword()));
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setFullname(dto.getFullname());
        entity.setBirthdate(dto.getBirthdate());
        entity.setDni(dto.getDni());
        entity.setPhotoUrl(null);
        entity.setPhotoKeyName(null);
        return entity;
    }

    public UserResponseDTO toResponse(UserEntity entity) {
        return new UserResponseDTO(
                entity.getId(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getFullname(),
                entity.getBirthdate(),
                entity.getDni(),
                entity.getPhotoUrl(),
                entity.getRole()
        );
    }
}
