package com.app.teleticket.qr.service;

import com.app.teleticket.qr.dto.QrPayload;

public interface QrCodeService {

    byte[] generate(Integer userId, Integer eventId);

    QrPayload decode(byte[] bytes, String contentType);
}