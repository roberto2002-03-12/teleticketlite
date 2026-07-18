package com.app.teleticket.users.service.impl;

import com.app.teleticket.users.dto.DisaffiliateStaffEventRequest;
import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.dto.UserStaffCreateDTO;
import com.app.teleticket.users.entity.StaffEntity;
import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.StaffRepository;
import com.app.teleticket.users.repository.UserRepository;
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
    UserRepository userRepository;

    @Inject
    UserMapper mapper;

    @Inject
    CognitoUserService cognito;

    @Inject
    UserPhotoStorageService photoStorage;

    @Override
    @Transactional
    public UserResponseDTO create(UserStaffCreateDTO dto, byte[] photo, String contentType) {
        UserEntity user = creation.create(dto, ROLE_STAFF, photo, contentType);
        try {
            persistAffiliation(user.getId(), dto.getEventId());
        } catch (RuntimeException e) {
            safeCognitoDelete(user.getEmail());
            safePhotoDelete(user.getPhotoKeyName());
            throw e;
        }
        return mapper.toResponse(user);
    }

    @Override
    @Transactional
    public void desaffiliate(DisaffiliateStaffEventRequest request) {
        Integer userId = request.getUserId();
        Integer eventId = request.getEventId();
        long deleted = staffRepository.deleteByUserAndEvent(userId, eventId);
        if (deleted == 0) {
            throw new UserException(404, "Staff affiliation not found");
        }
    }

    @Override
    @Transactional
    public void affiliate(DisaffiliateStaffEventRequest request) {
        Integer userId = request.getUserId();
        Integer eventId = request.getEventId();
        if (staffRepository.findByUserAndEvent(userId, eventId).isPresent()) {
            throw new UserException(409, "User is already affiliated to this event");
        }
        UserEntity user = userRepository.findById(userId.longValue());
        if (user == null) {
            throw new UserException(404, "User not found");
        }
        if (!ROLE_STAFF.equals(user.getRole())) {
            throw new UserException(400, "User is not a STAFF");
        }
        persistAffiliation(userId, eventId);
    }

    private void persistAffiliation(Integer userId, Integer eventId) {
        StaffEntity staff = new StaffEntity();
        staff.setUserId(userId);
        staff.setEventId(eventId);
        staffRepository.persist(staff);
        staffRepository.flush();
    }

    private void safeCognitoDelete(String email) {
        try {
            cognito.adminDeleteUser(email);
        } catch (RuntimeException ignored) {
            // mejor esfuerzo; se conserva el error original
        }
    }

    private void safePhotoDelete(String keyName) {
        if (keyName == null || keyName.isBlank()) {
            return;
        }
        try {
            photoStorage.delete(keyName);
        } catch (RuntimeException ignored) {
            // mejor esfuerzo; se conserva el error original
        }
    }
}
