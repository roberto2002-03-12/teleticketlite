package com.app.teleticket.qr.service.impl;

import com.app.teleticket.events.entity.EventEntity;
import com.app.teleticket.events.repository.EventRepository;
import com.app.teleticket.qr.dto.QrTicketResponseDTO;
import com.app.teleticket.qr.dto.QrUploadResult;
import com.app.teleticket.qr.entity.EventAssistantEntity;
import com.app.teleticket.qr.entity.QrTicketEntity;
import com.app.teleticket.qr.exception.QrException;
import com.app.teleticket.qr.repository.EventAssistantRepository;
import com.app.teleticket.qr.repository.QrTicketRepository;
import com.app.teleticket.qr.service.QrCodeService;
import com.app.teleticket.qr.service.QrRegisterService;
import com.app.teleticket.qr.service.QrStorageService;
import com.app.teleticket.users.entity.UserEntity;
import com.app.teleticket.users.exception.UserException;
import com.app.teleticket.users.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;

@ApplicationScoped
public class QrRegisterServiceImpl implements QrRegisterService {

    @Inject
    UserRepository userRepository;

    @Inject
    EventRepository eventRepository;

    @Inject
    EventAssistantRepository eventAssistantRepository;

    @Inject
    QrTicketRepository qrTicketRepository;

    @Inject
    QrCodeService qrCodeService;

    @Inject
    QrStorageService qrStorageService;

    @Override
    @Transactional
    public QrTicketResponseDTO register(String currentEmail, Integer eventId) {
        UserEntity user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UserException(404, "User not found"));
        EventEntity event = eventRepository.findById(eventId)
                .orElseThrow(() -> new QrException(404, "Event not found"));

        if (!event.isAvailable()) {
            throw new QrException(400, "Event is not available");
        }
        if (event.isFinished()) {
            throw new QrException(400, "Event is already finished");
        }
        if (eventAssistantRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new QrException(409, "User already registered for this event");
        }
        if (eventAssistantRepository.countByEventId(eventId) >= event.getMaxPeople()) {
            throw new QrException(409, "Event is full");
        }

        byte[] qrBytes = qrCodeService.generate(user.getId(), eventId);
        QrUploadResult uploaded = qrStorageService.upload(user.getId(), eventId, qrBytes);

        try {
            EventAssistantEntity assistant = new EventAssistantEntity();
            assistant.setUserId(user.getId());
            assistant.setEventId(eventId);
            assistant.setRegisterDate(LocalDateTime.now());
            eventAssistantRepository.persist(assistant);
            eventAssistantRepository.flush();

            QrTicketEntity ticket = new QrTicketEntity();
            ticket.setQrUrl(uploaded.url());
            ticket.setQrKey(uploaded.key());
            ticket.setAlreadyApplied(false);
            ticket.setUserId(user.getId());
            ticket.setEventId(eventId);
            qrTicketRepository.persist(ticket);
            qrTicketRepository.flush();

            return toResponse(ticket);
        } catch (RuntimeException e) {
            qrStorageService.delete(uploaded.key());
            throw e;
        }
    }

    private QrTicketResponseDTO toResponse(QrTicketEntity ticket) {
        return new QrTicketResponseDTO(
                ticket.getId(),
                ticket.getUserId(),
                ticket.getEventId(),
                ticket.getQrUrl(),
                ticket.isAlreadyApplied());
    }
}