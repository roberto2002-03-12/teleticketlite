package com.app.teleticket.qr.repository;

import com.app.teleticket.qr.entity.EventAssistantEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EventAssistantRepository implements PanacheRepository<EventAssistantEntity> {

    public Optional<EventAssistantEntity> findById(Integer id) {
        return find("id", id).firstResultOptional();
    }

    public Optional<EventAssistantEntity> findByUserIdAndEventId(Integer userId, Integer eventId) {
        return find("userId = ?1 and eventId = ?2", userId, eventId).firstResultOptional();
    }

    public boolean existsByUserIdAndEventId(Integer userId, Integer eventId) {
        return count("userId = ?1 and eventId = ?2", userId, eventId) > 0;
    }

    public long countByEventId(Integer eventId) {
        return count("eventId", eventId);
    }

    public List<EventAssistantEntity> findByEventId(Integer eventId) {
        return find("eventId", eventId).list();
    }
}