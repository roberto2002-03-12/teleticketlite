package com.app.teleticket.events.dto;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.List;

public class EventImageReplaceForm {

    @RestForm("photos")
    private List<FileUpload> photos;

    public List<FileUpload> getPhotos() {
        return photos;
    }

    public void setPhotos(List<FileUpload> photos) {
        this.photos = photos;
    }
}
