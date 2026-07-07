package com.app.teleticket.users.service.impl;

import com.app.teleticket.users.dto.UserResponseDTO;
import com.app.teleticket.users.dto.UserStaffCreateDTO;
import com.app.teleticket.users.entity.StaffEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.StaffRepository;
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

    @Override
    @Transactional
    public UserResponseDTO create(UserStaffCreateDTO dto, byte[] photo, String contentType) {
        var user = creation.create(dto, ROLE_STAFF, photo, contentType);
        affiliateStaff(user.id, dto.eventId);
        return mapper.toResponse(user);
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
