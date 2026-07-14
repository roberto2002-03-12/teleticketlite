package com.app.teleticket.qr.dto;

public record EventAssistantResponseDTO(
        int id,
        Integer userId,
        Integer eventId,
        java.time.LocalDateTime registerDate
) {
}