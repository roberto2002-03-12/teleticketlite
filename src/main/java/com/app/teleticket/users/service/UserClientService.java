package com.app.teleticket.users.service;

import com.app.teleticket.users.dto.UserCreateDTO;
import com.app.teleticket.users.dto.UserResponseDTO;

public interface UserClientService {

    UserResponseDTO create(UserCreateDTO dto, byte[] photo, String contentType);
}
