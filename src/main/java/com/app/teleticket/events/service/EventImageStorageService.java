package com.app.teleticket.events.service;

public interface EventImageStorageService {

    String upload(Integer eventId, Integer index, String contentType, byte[] bytes);

    void delete(String key);

    void deleteAll(java.util.List<String> keys);
}