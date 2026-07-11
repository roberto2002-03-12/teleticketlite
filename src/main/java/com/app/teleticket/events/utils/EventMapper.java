package com.app.teleticket.events.utils;

import com.app.teleticket.events.dto.EventCategoryResponseDTO;
import com.app.teleticket.events.dto.EventImageResponseDTO;
import com.app.teleticket.events.dto.EventResponseDTO;
import com.app.teleticket.events.dto.EventUpdateDTO;
import com.app.teleticket.events.entity.EventCategoryEntity;
import com.app.teleticket.events.entity.EventEntity;
import com.app.teleticket.events.entity.EventImageEntity;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class EventMapper {

    public EventResponseDTO toResponse(EventEntity entity, List<EventImageEntity> images) {
        List<EventImageResponseDTO> imageDtos = images.stream()
                .map(img -> new EventImageResponseDTO(img.getId(), img.getUrl(), img.getIndex()))
                .toList();
        return new EventResponseDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getMaxPeople(),
                entity.getAddress(),
                entity.isAvailable(),
                entity.isFinished(),
                entity.getStartDate(),
                entity.getFinishDate(),
                entity.getOwnerId(),
                entity.getCategoryId(),
                imageDtos
        );
    }

    public EventResponseDTO toResponse(EventEntity entity) {
        return toResponse(entity, List.of());
    }

    public EventCategoryResponseDTO toResponse(EventCategoryEntity entity) {
        return new EventCategoryResponseDTO(entity.getId(), entity.getName(), entity.getDescription());
    }

    public void applyUpdate(EventEntity entity, EventUpdateDTO dto) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setMaxPeople(dto.getMaxPeople());
        entity.setAddress(dto.getAddress());
        entity.setAvailable(dto.isAvailable());
        entity.setFinished(dto.isFinished());
        entity.setStartDate(dto.getStartDate());
        entity.setFinishDate(dto.getFinishDate());
        entity.setCategoryId(dto.getCategoryId());
    }
}