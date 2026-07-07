package com.app.teleticket.users.repository;

import com.app.teleticket.users.entity.StaffEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class StaffRepository implements PanacheRepository<StaffEntity> {

    public Optional<StaffEntity> findByUserAndEvent(Integer userId, Integer eventId) {
        return find("userId = ?1 AND eventId = ?2", userId, eventId).firstResultOptional();
    }

    public long deleteByUserAndEvent(Integer userId, Integer eventId) {
        return delete("userId = ?1 AND eventId = ?2", userId, eventId);
    }

    public long deleteByUser(Integer userId) {
        return delete("userId", userId);
    }

    public Integer findMaxIdStaff() {
        return find("ORDER BY idStaff DESC")
                .firstResultOptional()
                .map(s -> s.idStaff)
                .orElse(0);
    }
}
