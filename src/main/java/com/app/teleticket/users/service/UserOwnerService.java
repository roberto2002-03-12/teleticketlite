package com.app.teleticket.users.service;

import com.app.teleticket.users.dto.UserOwnerChangeDTO;
import com.app.teleticket.users.dto.UserOwnerCreateDTO;
import com.app.teleticket.users.dto.UserResponseDTO;

public interface UserOwnerService {

    UserResponseDTO create(UserOwnerCreateDTO dto, byte[] photo, String contentType);

    UserResponseDTO changeToOwner(Long userId, UserOwnerChangeDTO dto);
}
