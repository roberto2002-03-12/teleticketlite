package com.app.teleticket.events.service;

import com.app.teleticket.events.dto.EventResponseDTO;
import com.app.teleticket.events.dto.EventStaffUpdateDTO;

import java.util.List;

public interface EventStaffService {

    List<EventResponseDTO> listAffiliated(String currentEmail);

    EventResponseDTO updateStaffFields(String currentEmail, Integer eventId, EventStaffUpdateDTO dto);
}