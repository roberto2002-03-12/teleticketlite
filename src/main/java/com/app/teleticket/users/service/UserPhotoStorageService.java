package com.app.teleticket.users.service;

public interface UserPhotoStorageService {

    String upload(Long userId, String contentType, byte[] bytes);

    void delete(String key);
}
