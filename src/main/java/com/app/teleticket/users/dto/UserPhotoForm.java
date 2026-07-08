package com.app.teleticket.users.dto;

import jakarta.validation.constraints.NotNull;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class UserPhotoForm {

    @RestForm("photo")
    @NotNull
    private FileUpload photo;

    public FileUpload getPhoto() {
        return photo;
    }

    public void setPhoto(FileUpload photo) {
        this.photo = photo;
    }
}
