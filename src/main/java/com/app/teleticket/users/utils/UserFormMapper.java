package com.app.teleticket.users.utils;

import com.app.teleticket.users.dto.UserCreateDTO;
import com.app.teleticket.users.dto.UserCreateForm;
import com.app.teleticket.users.dto.UserOwnerChangeDTO;
import com.app.teleticket.users.dto.UserOwnerChangeForm;
import com.app.teleticket.users.dto.UserOwnerCreateDTO;
import com.app.teleticket.users.dto.UserOwnerCreateForm;
import com.app.teleticket.users.dto.UserStaffCreateDTO;
import com.app.teleticket.users.dto.UserStaffCreateForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

/**
 * Converts multipart form beans into the internal DTOs consumed by the user services,
 * and extracts raw photo bytes so no {@link FileUpload} ever reaches the service layer.
 */
public final class UserFormMapper {

    private UserFormMapper() {
    }

    public static UserCreateDTO toCreateDTO(UserCreateForm form) {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setEmail(form.getEmail());
        dto.setPassword(form.getPassword());
        dto.setPhoneNumber(form.getPhoneNumber());
        dto.setFullname(form.getFullname());
        dto.setBirthdate(form.getBirthdate());
        dto.setDni(form.getDni());
        return dto;
    }

    public static UserStaffCreateDTO toStaffCreateDTO(UserStaffCreateForm form) {
        UserStaffCreateDTO dto = new UserStaffCreateDTO();
        dto.setEmail(form.getEmail());
        dto.setPassword(form.getPassword());
        dto.setPhoneNumber(form.getPhoneNumber());
        dto.setFullname(form.getFullname());
        dto.setBirthdate(form.getBirthdate());
        dto.setDni(form.getDni());
        dto.setEventId(form.getEventId());
        return dto;
    }

    public static UserOwnerCreateDTO toOwnerCreateDTO(UserOwnerCreateForm form) {
        UserOwnerCreateDTO dto = new UserOwnerCreateDTO();
        dto.setEmail(form.getEmail());
        dto.setPassword(form.getPassword());
        dto.setPhoneNumber(form.getPhoneNumber());
        dto.setFullname(form.getFullname());
        dto.setBirthdate(form.getBirthdate());
        dto.setDni(form.getDni());
        dto.setRuc(form.getRuc());
        return dto;
    }

    public static UserOwnerChangeDTO toOwnerChangeDTO(UserOwnerChangeForm form) {
        UserOwnerChangeDTO dto = new UserOwnerChangeDTO();
        dto.setRuc(form.getRuc());
        return dto;
    }

    public static byte[] photoBytes(FileUpload photo) {
        if (photo == null) {
            return null;
        }
        try {
            return Files.readAllBytes(photo.uploadedFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String photoContentType(FileUpload photo) {
        return photo == null ? null : photo.contentType();
    }
}
