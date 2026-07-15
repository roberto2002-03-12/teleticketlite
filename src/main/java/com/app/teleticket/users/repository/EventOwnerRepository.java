package com.app.teleticket.users.repository;

import com.app.teleticket.users.entity.EventOwnerEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class EventOwnerRepository implements PanacheRepository<EventOwnerEntity> {

    public Optional<EventOwnerEntity> findById(Integer id) {
        return find("idEventOwner", id).firstResultOptional();
    }

    public Optional<EventOwnerEntity> findByUserId(Integer userId) {
        return find("userId", userId).firstResultOptional();
    }

    public long deleteByUserId(Integer userId) {
        return delete("userId", userId);
    }
}
