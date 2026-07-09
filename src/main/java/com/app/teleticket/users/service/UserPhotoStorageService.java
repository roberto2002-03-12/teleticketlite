package com.app.teleticket.users.service;

public interface UserPhotoStorageService {

    String upload(Integer userId, String contentType, byte[] bytes);

    void delete(String key);
}
