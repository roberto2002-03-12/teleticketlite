package com.app.teleticket.events.service;

import com.app.teleticket.events.dto.EventCreateDTO;
import com.app.teleticket.events.dto.EventResponseDTO;
import com.app.teleticket.events.dto.EventUpdateDTO;

import java.util.List;

public interface EventOwnerService {

    EventResponseDTO create(String currentEmail, EventCreateDTO dto);

    EventResponseDTO update(String currentEmail, Integer eventId, EventUpdateDTO dto);

    EventResponseDTO cancel(String currentEmail, Integer eventId);

    List<EventResponseDTO> listOwn(String currentEmail);
}