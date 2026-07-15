package com.app.teleticket.events.service.impl;

import com.app.teleticket.events.dto.EventResponseDTO;
import com.app.teleticket.events.dto.PageResponse;
import com.app.teleticket.events.entity.EventEntity;
import com.app.teleticket.events.entity.EventImageEntity;
import com.app.teleticket.events.exception.EventException;
import com.app.teleticket.events.repository.EventCategoryRepository;
import com.app.teleticket.events.repository.EventImageRepository;
import com.app.teleticket.events.repository.EventRepository;
import com.app.teleticket.events.service.EventClientService;
import com.app.teleticket.events.utils.EventMapper;
import com.app.teleticket.users.entity.EventOwnerEntity;
import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.repository.EventOwnerRepository;
import com.app.teleticket.users.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class EventClientServiceImpl implements EventClientService {

    private static final int PAGE_SIZE = 12;

    @Inject
    EventRepository eventRepository;

    @Inject
    EventImageRepository eventImageRepository;

    @Inject
    EventCategoryRepository eventCategoryRepository;

    @Inject
    EventOwnerRepository eventOwnerRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    EventMapper mapper;

    @Override
    public PageResponse<EventResponseDTO> search(String title,
                                                  LocalDateTime startDate,
                                                  LocalDateTime finishDate,
                                                  Integer categoryId,
                                                  int pageIndex) {
        long total = eventRepository.countActive(title, startDate, finishDate, categoryId);
        int totalPages = (int) Math.ceil((double) total / PAGE_SIZE);
        if (total == 0) {
            totalPages = 1;
        }
        if (pageIndex < 0 || (pageIndex >= totalPages && total > 0)) {
            throw new EventException(400, "Page " + pageIndex + " is out of range (0.." + (totalPages - 1) + ")");
        }

        List<EventEntity> events = eventRepository.searchActive(title, startDate, finishDate, categoryId, pageIndex, PAGE_SIZE);
        List<EventResponseDTO> items = events.stream()
                .map(e -> mapper.toResponse(e, eventImageRepository.findByEventId(e.getId()),
                        resolveOwnerFullName(e.getOwnerId()), resolveCategoryName(e.getCategoryId())))
                .toList();

        return new PageResponse<>(items, total, pageIndex, PAGE_SIZE, totalPages);
    }

    @Override
    public EventResponseDTO getActiveById(Integer eventId) {
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(404, "Event not found"));
        if (!event.isAvailable()) {
            throw new EventException(404, "Event not found");
        }
        List<EventImageEntity> images = eventImageRepository.findByEventId(eventId);
        return mapper.toResponse(event, images,
                resolveOwnerFullName(event.getOwnerId()),
                resolveCategoryName(event.getCategoryId()));
    }

    private String resolveOwnerFullName(Integer ownerId) {
        return eventOwnerRepository.findById(ownerId)
                .map(EventOwnerEntity::getUserId)
                .flatMap(userRepository::findById)
                .map(UserEntity::getFullname)
                .orElse(null);
    }

    private String resolveCategoryName(Integer categoryId) {
        return eventCategoryRepository.findById(categoryId)
                .map(c -> c.getName())
                .orElse(null);
    }
}