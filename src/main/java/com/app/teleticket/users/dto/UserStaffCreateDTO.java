package com.app.teleticket.users.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class UserStaffCreateDTO extends UserCreateDTO {

    @NotNull
    @Positive
    public Integer eventId;
}
