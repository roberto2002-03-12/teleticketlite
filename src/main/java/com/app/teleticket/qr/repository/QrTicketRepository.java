package com.app.teleticket.qr.repository;

import com.app.teleticket.qr.entity.QrTicketEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class QrTicketRepository implements PanacheRepository<QrTicketEntity> {

    public Optional<QrTicketEntity> findById(Integer id) {
        return find("id", id).firstResultOptional();
    }

    public Optional<QrTicketEntity> findByUserIdAndEventId(Integer userId, Integer eventId) {
        return find("userId = ?1 and eventId = ?2", userId, eventId).firstResultOptional();
    }

    public List<QrTicketEntity> listTicketsByUserId(Integer userId) {
        return list("userId", userId);
    }
}