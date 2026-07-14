package com.app.teleticket.events.dto;

import java.time.LocalDateTime;
import java.util.Date;

public class EventObjForQrList {
    private Integer id;
    private String eventName;
    private String eventAddress;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;

    public EventObjForQrList(Integer id, String eventName, String eventAddress, LocalDateTime startDate, LocalDateTime finishDate) {
        this.id = id;
        this.eventName = eventName;
        this.eventAddress = eventAddress;
        this.startDate = startDate;
        this.finishDate = finishDate;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventAddress() {
        return eventAddress;
    }
    public void setEventAddress(String eventAddress) {
        this.eventAddress = eventAddress;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getFinishDate() {
        return finishDate;
    }
    public void setFinishDate(LocalDateTime finishDate) {
        this.finishDate = finishDate;
    }
}
