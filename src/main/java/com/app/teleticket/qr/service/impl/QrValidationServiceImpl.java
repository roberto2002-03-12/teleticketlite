package com.app.teleticket.qr.service.impl;

import com.app.teleticket.qr.dto.QrPayload;
import com.app.teleticket.qr.dto.QrValidationResponseDTO;
import com.app.teleticket.qr.entity.QrTicketEntity;
import com.app.teleticket.qr.exception.QrException;
import com.app.teleticket.qr.repository.QrTicketRepository;
import com.app.teleticket.qr.service.QrCodeService;
import com.app.teleticket.qr.service.QrValidationService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class QrValidationServiceImpl implements QrValidationService {

    @Inject
    QrCodeService qrCodeService;

    @Inject
    QrTicketRepository qrTicketRepository;

    @Override
    @Transactional
    public QrValidationResponseDTO validate(byte[] qrBytes, String contentType) {
        try {
            QrPayload payload = qrCodeService.decode(qrBytes, contentType);
            Optional<QrTicketEntity> ticketOpt = qrTicketRepository.findByUserIdAndEventId(
                    payload.userId(), payload.eventId());
            if (ticketOpt.isEmpty()) {
                return new QrValidationResponseDTO(false);
            }
            QrTicketEntity ticket = ticketOpt.get();
            if (ticket.isAlreadyApplied()) {
                return new QrValidationResponseDTO(false);
            }
            ticket.setAlreadyApplied(true);
            return new QrValidationResponseDTO(true);
        } catch (QrException e) {
            return new QrValidationResponseDTO(false);
        }
    }
}