package com.app.teleticket.users.service;

import com.app.teleticket.users.dto.DisaffiliateStaffEventRequest;
import com.app.teleticket.users.dto.UserStaffCreateDTO;
import com.app.teleticket.users.dto.UserResponseDTO;

public interface UserStaffService {

    UserResponseDTO create(UserStaffCreateDTO dto, byte[] photo, String contentType);

    void desaffiliate(DisaffiliateStaffEventRequest request);

    void affiliate(DisaffiliateStaffEventRequest request);
}
