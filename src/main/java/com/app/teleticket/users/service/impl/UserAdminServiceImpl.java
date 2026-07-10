package com.app.teleticket.users.service.impl;

import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.EventOwnerRepository;
import com.app.teleticket.users.repository.StaffRepository;
import com.app.teleticket.users.repository.UserRepository;
import com.app.teleticket.users.service.CognitoUserService;
import com.app.teleticket.users.service.UserAdminService;
import com.app.teleticket.users.service.UserPhotoStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserAdminServiceImpl implements UserAdminService {

    @Inject
    UserRepository userRepository;

    @Inject
    StaffRepository staffRepository;

    @Inject
    EventOwnerRepository eventOwnerRepository;

    @Inject
    CognitoUserService cognito;

    @Inject
    UserPhotoStorageService photoStorage;

    @Override
    @Transactional
    public void deleteAccount(Long userId) {
        UserEntity user = userRepository.findById(userId);
        if (user == null) {
            throw new UserException(404, "User not found");
        }
        staffRepository.deleteByUser(user.getId().intValue());
        eventOwnerRepository.deleteByUserId(user.getId());
        if (user.getPhotoKeyName() != null) {
            photoStorage.delete(user.getPhotoKeyName());
        }
        userRepository.delete(user);
        cognito.adminDeleteUser(user.getEmail());
    }
}
