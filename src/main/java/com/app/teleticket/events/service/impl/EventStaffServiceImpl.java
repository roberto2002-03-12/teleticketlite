package com.app.teleticket.events.service.impl;

import com.app.teleticket.events.dto.EventResponseDTO;
import com.app.teleticket.events.dto.EventStaffUpdateDTO;
import com.app.teleticket.events.entity.EventEntity;
import com.app.teleticket.events.entity.EventImageEntity;
import com.app.teleticket.events.exception.EventException;
import com.app.teleticket.events.repository.EventCategoryRepository;
import com.app.teleticket.events.repository.EventImageRepository;
import com.app.teleticket.events.repository.EventRepository;
import com.app.teleticket.events.service.EventStaffService;
import com.app.teleticket.events.utils.EventMapper;
import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.StaffRepository;
import com.app.teleticket.users.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class EventStaffServiceImpl implements EventStaffService {

    @Inject
    EventRepository eventRepository;

    @Inject
    EventImageRepository eventImageRepository;

    @Inject
    EventCategoryRepository eventCategoryRepository;

    @Inject
    StaffRepository staffRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    EventMapper mapper;

    @Override
    public List<EventResponseDTO> listAffiliated(String currentEmail) {
        Integer userId = resolveUserId(currentEmail);
        List<EventEntity> events = eventRepository.findEventsForStaffUser(userId);
        return events.stream()
                .map(e -> mapper.toResponse(e, eventImageRepository.findByEventId(e.getId())))
                .toList();
    }

    @Override
    @Transactional
    public EventResponseDTO updateStaffFields(String currentEmail, Integer eventId, EventStaffUpdateDTO dto) {
        Integer userId = resolveUserId(currentEmail);

        if (staffRepository.findByUserAndEvent(userId, eventId).isEmpty()) {
            throw new EventException(403, "You can only edit events you are affiliated with");
        }
        if (eventCategoryRepository.findById(dto.getCategoryId()).isEmpty()) {
            throw new EventException(404, "Category not found");
        }

        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(404, "Event not found"));
        event.setDescription(dto.getDescription());
        event.setCategoryId(dto.getCategoryId());

        List<EventImageEntity> images = eventImageRepository.findByEventId(eventId);
        return mapper.toResponse(event, images);
    }

    private Integer resolveUserId(String currentEmail) {
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserException(404, "User not found"));
        return user.getId();
    }
}