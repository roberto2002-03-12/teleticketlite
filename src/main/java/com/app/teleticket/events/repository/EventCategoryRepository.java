package com.app.teleticket.events.repository;

import com.app.teleticket.events.entity.EventCategoryEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class EventCategoryRepository implements PanacheRepository<EventCategoryEntity> {

    public Optional<EventCategoryEntity> findById(Integer id) {
        return find("id", id).firstResultOptional();
    }

    public Optional<EventCategoryEntity> findByName(String name) {
        return find("name", name).firstResultOptional();
    }
}