package com.app.teleticket.users.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "staff")
@IdClass(StaffEntity.StaffId.class)
public class StaffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_staff")
    private Integer idStaff;

    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "event_id")
    private Integer eventId;

    public StaffEntity() {
    }

    public StaffEntity(Integer idStaff, Integer userId, Integer eventId) {
        this.idStaff = idStaff;
        this.userId = userId;
        this.eventId = eventId;
    }

    public static class StaffId implements Serializable {
        public Integer idStaff;
        public Integer userId;
        public Integer eventId;

        public StaffId() {
        }

        public StaffId(Integer idStaff, Integer userId, Integer eventId) {
            this.idStaff = idStaff;
            this.userId = userId;
            this.eventId = eventId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StaffId that)) return false;
            return Objects.equals(idStaff, that.idStaff)
                    && Objects.equals(userId, that.userId)
                    && Objects.equals(eventId, that.eventId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(idStaff, userId, eventId);
        }
    }

    public Integer getIdStaff() {
        return idStaff;
    }

    public void setIdStaff(Integer idStaff) {
        this.idStaff = idStaff;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }
}
