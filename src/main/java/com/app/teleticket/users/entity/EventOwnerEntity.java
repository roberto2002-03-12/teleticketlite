package com.app.teleticket.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_owner")
public class EventOwnerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_event_owner")
    private Integer idEventOwner;

    @Column(nullable = false, length = 15)
    private String ruc;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    public EventOwnerEntity() {
    }

    public EventOwnerEntity(String ruc, boolean enabled, Integer userId) {
        this.ruc = ruc;
        this.enabled = enabled;
        this.userId = userId;
    }

    public Integer getIdEventOwner() {
        return idEventOwner;
    }

    public void setIdEventOwner(Integer idEventOwner) {
        this.idEventOwner = idEventOwner;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
