package com.app.teleticket.users.dto;

import jakarta.validation.constraints.NotBlank;

public class DisaffiliateStaffEventRequest {
    @NotBlank
    int userId;

    @NotBlank
    int eventId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}
