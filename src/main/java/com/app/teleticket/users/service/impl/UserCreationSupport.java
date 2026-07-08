package com.app.teleticket.users.service.impl;

import com.app.teleticket.users.dto.UserCreateDTO;
import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.UserRepository;
import com.app.teleticket.users.service.CognitoUserService;
import com.app.teleticket.users.service.UserPhotoStorageService;
import com.app.teleticket.users.utils.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Set;

@ApplicationScoped
public class UserCreationSupport {

    private static final Set<String> ALLOWED_PHOTO_TYPES = Set.of("image/jpeg", "image/png");

    @Inject
    UserRepository UserRepository;

    @Inject
    UserMapper mapper;

    @Inject
    CognitoUserService cognito;

    @Inject
    UserPhotoStorageService photoStorage;

    @Transactional
    public UserEntity create(UserCreateDTO dto, String role, byte[] photo, String contentType) {
        assertUnique(dto);
        cognito.adminCreateUser(dto.getEmail(), dto.getPhoneNumber(), role, dto.getPassword());
        UserEntity entity;
        try {
            entity = mapper.toEntity(dto);
            entity.setRole(role);
            UserRepository.persist(entity);
            UserRepository.flush();
            if (photo != null && photo.length > 0) {
                storePhoto(entity, photo, contentType);
                UserRepository.flush();
            }
        } catch (RuntimeException e) {
            try {
                cognito.adminDeleteUser(dto.getEmail());
            } catch (RuntimeException cleanup) {
                // best-effort; original error retained
            }
            throw e;
        }
        return entity;
    }

    private void storePhoto(UserEntity entity, byte[] bytes, String contentType) {
        if (contentType == null || !ALLOWED_PHOTO_TYPES.contains(contentType)) {
            throw new UserException(415, "Only jpg, jpeg and png images are allowed");
        }
        String url = photoStorage.upload(entity.getId(), contentType, bytes);
        entity.setPhotoUrl(url);
        entity.setPhotoKeyName(extractKey(url));
    }

    private void assertUnique(UserCreateDTO dto) {
        if (UserRepository.existsByEmail(dto.getEmail())) {
            throw new UserException(409, "Email already in use");
        }
        if (UserRepository.existsByDni(dto.getDni())) {
            throw new UserException(409, "DNI already in use");
        }
        if (UserRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new UserException(409, "Phone number already in use");
        }
    }

    private String extractKey(String url) {
        int idx = url.indexOf(".amazonaws.com/");
        return idx < 0 ? url : url.substring(idx + ".amazonaws.com/".length());
    }
}
