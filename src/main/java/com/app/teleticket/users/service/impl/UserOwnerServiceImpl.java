package com.app.teleticket.users.service.impl;

import com.app.teleticket.users.dto.UserOwnerChangeDTO;
import com.app.teleticket.users.dto.UserOwnerCreateDTO;
import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.entity.EventOwnerEntity;
import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.EventOwnerRepository;
import com.app.teleticket.users.repository.UserRepository;
import com.app.teleticket.users.service.CognitoUserService;
import com.app.teleticket.users.service.UserOwnerService;
import com.app.teleticket.users.utils.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserOwnerServiceImpl implements UserOwnerService {

    private static final String ROLE_OWNER = "OWNER";

    @Inject
    UserCreationSupport creation;

    @Inject
    EventOwnerRepository eventOwnerRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    CognitoUserService cognito;

    @Inject
    UserMapper mapper;

    @Override
    @Transactional
    public UserResponseDTO create(UserOwnerCreateDTO dto, byte[] photo, String contentType) {
        var user = creation.create(dto, ROLE_OWNER, photo, contentType);
        try {
            persistEventOwner(user.getId(), dto.getRuc());
        } catch (RuntimeException e) {
            // El usuario de Cognito ya fue creado por creation.create();
            // compensar eliminándolo antes de relanzar.
            safeCognitoDelete(user.getEmail());
            throw e;
        }
        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponseDTO changeToOwner(Long userId, UserOwnerChangeDTO dto) {
        UserEntity user = userRepository.findById(userId);
        if (user == null) {
            throw new UserException(404, "User not found");
        }
        if (eventOwnerRepository.findByUserId(user.getId()).isPresent()) {
            throw new UserException(409, "User is already registered as an event owner");
        }

        boolean roleChanged = !ROLE_OWNER.equals(user.getRole());
        if (roleChanged) {
            user.setRole(ROLE_OWNER);
        }
        persistEventOwner(user.getId(), dto.getRuc());

        if (roleChanged) {
            safeAddToGroup(user.getEmail(), ROLE_OWNER);
        }
        return mapper.toResponse(user);
    }

    private void persistEventOwner(Integer userId, String ruc) {
        EventOwnerEntity row = new EventOwnerEntity(ruc, true, userId);
        eventOwnerRepository.persist(row);
        eventOwnerRepository.flush();
    }

    private void safeCognitoDelete(String email) {
        try {
            cognito.adminDeleteUser(email);
        } catch (RuntimeException ignored) {
            // mejor esfuerzo; se conserva el error original
        }
    }

    private void safeAddToGroup(String email, String group) {
        try {
            cognito.addToGroup(email, group);
        } catch (RuntimeException ignored) {
            // mejor esfuerzo; se conserva el error original
        }
    }
}
