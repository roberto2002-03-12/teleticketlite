package com.app.teleticket.events.service.impl;

import com.app.teleticket.events.dto.EventCategoryCreateDTO;
import com.app.teleticket.events.dto.EventCategoryResponseDTO;
import com.app.teleticket.events.entity.EventCategoryEntity;
import com.app.teleticket.events.exception.EventException;
import com.app.teleticket.events.repository.EventCategoryRepository;
import com.app.teleticket.events.service.EventCategoryService;
import com.app.teleticket.events.utils.EventMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class EventCategoryServiceImpl implements EventCategoryService {

    @Inject
    EventCategoryRepository eventCategoryRepository;

    @Inject
    EventMapper mapper;

    @Override
    @Transactional
    public EventCategoryResponseDTO create(EventCategoryCreateDTO dto) {
        if (eventCategoryRepository.findByName(dto.getName()).isPresent()) {
            throw new EventException(409, "Category name already in use");
        }
        EventCategoryEntity entity = new EventCategoryEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        eventCategoryRepository.persist(entity);
        eventCategoryRepository.flush();
        return mapper.toResponse(entity);
    }

    @Override
    public List<EventCategoryResponseDTO> list() {
        return eventCategoryRepository.listAll().stream()
                .map(mapper::toResponse)
                .toList();
    }
}