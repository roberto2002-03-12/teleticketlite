package com.app.teleticket.users.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.jboss.resteasy.reactive.RestForm;

public class DisaffiliateStaffEventRequest {

    @NotNull
    @Positive
    private Integer userId;

    @NotNull
    @Positive
    private Integer eventId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }
}
