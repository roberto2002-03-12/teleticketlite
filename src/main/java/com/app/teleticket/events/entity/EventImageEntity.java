package com.app.teleticket.events.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "event_images")
public class EventImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_event_img")
    private int id;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(name = "key", nullable = false, length = 105)
    private String keyName;

    @Column(name = "index", nullable = false)
    private Integer index;

    @Column(name = "event_id", nullable = false)
    private Integer eventId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }
}