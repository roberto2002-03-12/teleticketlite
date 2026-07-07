package com.app.teleticket.users.service.impl;

import com.app.teleticket.users.dto.UserCreateDTO;
import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.service.UserClientService;
import com.app.teleticket.users.utils.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserClientServiceImpl implements UserClientService {

    private static final String ROLE_CLIENT = "CLIENT";

    @Inject
    UserCreationSupport creation;

    @Inject
    UserMapper mapper;

    @Override
    public UserResponseDTO create(UserCreateDTO dto, byte[] photo, String contentType) {
        return mapper.toResponse(creation.create(dto, ROLE_CLIENT, photo, contentType));
    }
}
