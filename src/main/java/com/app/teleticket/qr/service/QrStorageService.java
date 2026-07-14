package com.app.teleticket.qr.service;

import com.app.teleticket.qr.dto.QrUploadResult;

public interface QrStorageService {

    QrUploadResult upload(Integer userId, Integer eventId, byte[] bytes);

    void delete(String key);
}