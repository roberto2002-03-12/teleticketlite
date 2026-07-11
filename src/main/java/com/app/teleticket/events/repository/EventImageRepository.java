package com.app.teleticket.events.repository;

import com.app.teleticket.events.entity.EventImageEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class EventImageRepository implements PanacheRepository<EventImageEntity> {

    public List<EventImageEntity> findByEventId(Integer eventId) {
        return find("eventId", eventId).list();
    }

    public long deleteByEventId(Integer eventId) {
        return delete("eventId", eventId);
    }
}