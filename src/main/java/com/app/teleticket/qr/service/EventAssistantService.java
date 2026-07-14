package com.app.teleticket.qr.service;

import com.app.teleticket.qr.dto.EventAssistantResponseDTO;

import java.util.List;

public interface EventAssistantService {

    List<EventAssistantResponseDTO> listAssistants(String currentEmail, Integer eventId);
}