package com.app.teleticket.events.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class EventImagesDeleteRequest {

    @NotEmpty
    private List<Integer> imagesId;

    public List<Integer> getImagesId() {
        return imagesId;
    }

    public void setImagesId(List<Integer> imagesId) {
        this.imagesId = imagesId;
    }
}
