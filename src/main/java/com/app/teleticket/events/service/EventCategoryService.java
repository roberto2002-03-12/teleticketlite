package com.app.teleticket.events.service;

import com.app.teleticket.events.dto.EventCategoryCreateDTO;
import com.app.teleticket.events.dto.EventCategoryResponseDTO;

import java.util.List;

public interface EventCategoryService {

    EventCategoryResponseDTO create(EventCategoryCreateDTO dto);

    List<EventCategoryResponseDTO> list();
}