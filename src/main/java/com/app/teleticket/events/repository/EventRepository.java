package com.app.teleticket.events.repository;

import com.app.teleticket.events.entity.EventEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class EventRepository implements PanacheRepository<EventEntity> {

    public Optional<EventEntity> findById(Integer id) {
        return find("id", id).firstResultOptional();
    }

    public List<EventEntity> findByOwnerId(Integer ownerId) {
        return find("ownerId", ownerId).list();
    }

    public List<EventEntity> findEventsForStaffUser(Integer userId) {
        return find("FROM EventEntity e WHERE e.id IN " +
                "(SELECT s.eventId FROM StaffEntity s WHERE s.userId = ?1)", userId).list();
    }

    public List<EventEntity> searchActive(String title,
                                           LocalDateTime startDate,
                                           LocalDateTime finishDate,
                                           Integer categoryId,
                                           int pageIndex,
                                           int pageSize) {
        Map<String, Object> params = new HashMap<>();
        String query = buildActiveQuery(title, startDate, finishDate, categoryId, params, true);
        return find(query, params).page(Page.of(pageIndex, pageSize)).list();
    }

    public long countActive(String title,
                            LocalDateTime startDate,
                            LocalDateTime finishDate,
                            Integer categoryId) {
        Map<String, Object> params = new HashMap<>();
        String query = buildActiveQuery(title, startDate, finishDate, categoryId, params, false);
        return count(query, params);
    }

    private String buildActiveQuery(String title,
                                    LocalDateTime startDate,
                                    LocalDateTime finishDate,
                                    Integer categoryId,
                                    Map<String, Object> params,
                                    boolean orderBy) {
        StringBuilder query = new StringBuilder("available = true");

        if (title != null && !title.isBlank()) {
            query.append(" AND lower(title) LIKE lower(:title)");
            params.put("title", "%" + title + "%");
        }
        if (startDate != null) {
            query.append(" AND startDate >= :startDate");
            params.put("startDate", startDate);
        }
        if (finishDate != null) {
            query.append(" AND finishDate <= :finishDate");
            params.put("finishDate", finishDate);
        }
        if (categoryId != null) {
            query.append(" AND categoryId = :categoryId");
            params.put("categoryId", categoryId);
        }
        if (orderBy) {
            query.append(" ORDER BY startDate ASC");
        }
        return query.toString();
    }
}