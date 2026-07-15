package com.app.teleticket.events.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponseDTO(
        int id,
        String title,
        String description,
        Integer maxPeople,
        String address,
        boolean available,
        boolean finished,
        LocalDateTime startDate,
        LocalDateTime finishDate,
        Integer ownerId,
        Integer categoryId,
        String ownerFullName,
        String categoryName,
        List<EventImageResponseDTO> images
) {
}