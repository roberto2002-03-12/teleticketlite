package com.app.teleticket.qr.dto;

import java.time.LocalDateTime;
import java.util.Date;

public record MyInscriptionsDTO(
        String qrUrl,
        boolean alreadyApplied,
        String eventName,
        String eventAddress,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
