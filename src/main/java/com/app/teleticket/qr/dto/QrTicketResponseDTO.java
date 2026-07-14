package com.app.teleticket.qr.dto;

public record QrTicketResponseDTO(
        int id,
        Integer userId,
        Integer eventId,
        String qrUrl,
        boolean alreadyApplied
) {
}