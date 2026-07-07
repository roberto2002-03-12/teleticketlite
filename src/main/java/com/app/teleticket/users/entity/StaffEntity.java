package com.app.teleticket.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "staff")
@IdClass(StaffEntity.StaffId.class)
public class StaffEntity {

    @Id
    @Column(name = "id_staff")
    public Integer idStaff;

    @Id
    @Column(name = "user_id")
    public Integer userId;

    @Id
    @Column(name = "event_id")
    public Integer eventId;

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
}
