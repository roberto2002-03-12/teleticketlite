package com.app.teleticket.users.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class UserPhotoForm {

    @FormParam("photo")
    @NotNull
    public FileUpload photo;
}
