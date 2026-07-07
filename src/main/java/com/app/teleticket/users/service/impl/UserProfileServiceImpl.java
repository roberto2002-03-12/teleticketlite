package com.app.teleticket.users.service.impl;

import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.dto.UserUpdateDTO;
import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.UserRepository;
import com.app.teleticket.users.service.UserProfileService;
import com.app.teleticket.users.service.UserPhotoStorageService;
import com.app.teleticket.users.utils.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Set;

@ApplicationScoped
public class UserProfileServiceImpl implements UserProfileService {

    private static final Set<String> ALLOWED_PHOTO_TYPES = Set.of("image/jpeg", "image/png");

    @Inject
    UserRepository UserRepository;

    @Inject
    UserPhotoStorageService photoStorage;

    @Inject
    UserMapper mapper;

    @Override
    public UserResponseDTO getMe(String email) {
        return mapper.toResponse(findByEmail(email));
    }

    @Override
    @Transactional
    public UserResponseDTO updateMe(String email, UserUpdateDTO dto) {
        UserEntity user = findByEmail(email);

        if (!user.dni.equals(dto.dni) && UserRepository.existsByDni(dto.dni)) {
            throw new UserException(409, "DNI already in use");
        }
        if (!user.phoneNumber.equals(dto.phoneNumber)
                && UserRepository.existsByPhoneNumber(dto.phoneNumber)) {
            throw new UserException(409, "Phone number already in use");
        }

        user.fullname = dto.fullname;
        user.phoneNumber = dto.phoneNumber;
        user.birthdate = dto.birthdate;
        user.dni = dto.dni;
        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponseDTO uploadPhoto(String email, byte[] bytes, String contentType) {
        UserEntity user = findByEmail(email);
        if (contentType == null || !ALLOWED_PHOTO_TYPES.contains(contentType)) {
            throw new UserException(415, "Only jpg, jpeg and png images are allowed");
        }
        if (bytes == null || bytes.length == 0) {
            throw new UserException(400, "Empty file");
        }
        if (user.photoKeyName != null) {
            photoStorage.delete(user.photoKeyName);
        }
        String url = photoStorage.upload(user.id, contentType, bytes);
        user.photoUrl = url;
        user.photoKeyName = extractKey(url);
        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public void deletePhoto(String email) {
        UserEntity user = findByEmail(email);
        if (user.photoKeyName != null) {
            photoStorage.delete(user.photoKeyName);
            user.photoKeyName = null;
            user.photoUrl = null;
        }
    }

    private UserEntity findByEmail(String email) {
        return UserRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(404, "User not found"));
    }

    private String extractKey(String url) {
        int idx = url.indexOf(".amazonaws.com/");
        return idx < 0 ? url : url.substring(idx + ".amazonaws.com/".length());
    }
}
