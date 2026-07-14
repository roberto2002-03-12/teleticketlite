package com.app.teleticket.qr.service;

import com.app.teleticket.qr.dto.QrValidationResponseDTO;

public interface QrValidationService {

    QrValidationResponseDTO validate(byte[] qrBytes, String contentType);
}