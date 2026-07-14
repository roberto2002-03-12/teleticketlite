package com.app.teleticket.qr.service;

import com.app.teleticket.qr.dto.QrTicketResponseDTO;

public interface QrRegisterService {

    QrTicketResponseDTO register(String currentEmail, Integer eventId);
}