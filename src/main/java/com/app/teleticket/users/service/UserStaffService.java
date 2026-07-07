package com.app.teleticket.users.service;

import com.app.teleticket.users.dto.UserStaffCreateDTO;
import com.app.teleticket.users.dto.UserResponseDTO;

public interface UserStaffService {

    UserResponseDTO create(UserStaffCreateDTO dto, byte[] photo, String contentType);

    void desaffiliate(Integer userId, Integer eventId);
}
