package com.app.teleticket.users.service;

import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.dto.UserUpdateDTO;

public interface UserProfileService {

    UserResponseDTO getMe(String email);

    UserResponseDTO updateMe(String email, UserUpdateDTO dto);

    UserResponseDTO uploadPhoto(String email, byte[] bytes, String contentType);

    void deletePhoto(String email);
}
