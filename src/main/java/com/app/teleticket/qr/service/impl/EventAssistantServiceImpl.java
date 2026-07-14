package com.app.teleticket.qr.service.impl;

import com.app.teleticket.events.entity.EventEntity;
import com.app.teleticket.events.repository.EventRepository;
import com.app.teleticket.qr.dto.EventAssistantResponseDTO;
import com.app.teleticket.qr.entity.EventAssistantEntity;
import com.app.teleticket.qr.exception.QrException;
import com.app.teleticket.qr.repository.EventAssistantRepository;
import com.app.teleticket.qr.service.EventAssistantService;
import com.app.teleticket.users.entity.EventOwnerEntity;
import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.EventOwnerRepository;
import com.app.teleticket.users.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class EventAssistantServiceImpl implements EventAssistantService {

    @Inject
    UserRepository userRepository;

    @Inject
    EventOwnerRepository eventOwnerRepository;

    @Inject
    EventRepository eventRepository;

    @Inject
    EventAssistantRepository eventAssistantRepository;

    @Override
    public List<EventAssistantResponseDTO> listAssistants(String currentEmail, Integer eventId) {
        Integer ownerId = resolveEventOwnerId(currentEmail);
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new QrException(404, "Event not found"));
        if (!event.getOwnerId().equals(ownerId)) {
            throw new QrException(403, "You can only list assistants of events that you own");
        }
        return eventAssistantRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Integer resolveEventOwnerId(String currentEmail) {
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserException(404, "User not found"));
        EventOwnerEntity owner = eventOwnerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new QrException(403, "Current user is not an event owner"));
        return owner.getIdEventOwner();
    }

    private EventAssistantResponseDTO toResponse(EventAssistantEntity assistant) {
        return new EventAssistantResponseDTO(
                assistant.getId(),
                assistant.getUserId(),
                assistant.getEventId(),
                assistant.getRegisterDate());
    }
}