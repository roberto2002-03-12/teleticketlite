package com.app.teleticket.events.service;

import com.app.teleticket.events.dto.EventResponseDTO;
import com.app.teleticket.events.dto.PageResponse;

import java.time.LocalDateTime;

public interface EventClientService {

    PageResponse<EventResponseDTO> search(String title,
                                          LocalDateTime startDate,
                                          LocalDateTime finishDate,
                                          Integer categoryId,
                                          int pageIndex);

    EventResponseDTO getActiveById(Integer eventId);
}