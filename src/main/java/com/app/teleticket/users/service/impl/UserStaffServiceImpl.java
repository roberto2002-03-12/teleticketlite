package com.app.teleticket.users.service.impl;

import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.dto.UserStaffCreateDTO;
import com.app.teleticket.users.entity.StaffEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.StaffRepository;
import com.app.teleticket.users.service.CognitoUserService;
import com.app.teleticket.users.service.UserPhotoStorageService;
import com.app.teleticket.users.service.UserStaffService;
import com.app.teleticket.users.utils.UserMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserStaffServiceImpl implements UserStaffService {

    private static final String ROLE_STAFF = "STAFF";

    @Inject
    UserCreationSupport creation;

    @Inject
    StaffRepository staffRepository;

    @Inject
    UserMapper mapper;

    @Inject
    CognitoUserService cognito;

    @Inject
    UserPhotoStorageService photoStorage;

    @Override
    @Transactional
    public UserResponseDTO create(UserStaffCreateDTO dto, byte[] photo, String contentType) {
        var user = creation.create(dto, ROLE_STAFF, photo, contentType);
        try {
            affiliateStaff(user.getId(), dto.getEventId());
        } catch (RuntimeException e) {
            // DB rollback handled by @Transactional; AWS resources are not.
            // Compensate: roll back the Cognito user and any uploaded S3 photo
            // that were persisted by UserCreationSupport before the failure.
            safeCognitoDelete(user.getEmail());
            safePhotoDelete(user.getPhotoKeyName());
            throw e;
        }
        return mapper.toResponse(user);
    }

    private void safeCognitoDelete(String email) {
        try {
            cognito.adminDeleteUser(email);
        } catch (RuntimeException ignored) {
            // best-effort; original error retained
        }
    }

    private void safePhotoDelete(String keyName) {
        if (keyName == null || keyName.isBlank()) {
            return;
        }
        try {
            photoStorage.delete(keyName);
        } catch (RuntimeException ignored) {
            // best-effort; original error retained
        }
    }

    @Override
    @Transactional
    public void desaffiliate(Integer userId, Integer eventId) {
        long deleted = staffRepository.deleteByUserAndEvent(userId, eventId);
        if (deleted == 0) {
            throw new UserException(404, "Staff affiliation not found");
        }
    }

    private void affiliateStaff(Long userId, Integer eventId) {
        if (staffRepository.findByUserAndEvent(userId.intValue(), eventId).isPresent()) {
            throw new UserException(409, "User is already affiliated to this event");
        }
        Integer nextId = staffRepository.findMaxIdStaff() + 1;
        staffRepository.persist(new StaffEntity(nextId, userId.intValue(), eventId));
        staffRepository.flush();
    }
}
