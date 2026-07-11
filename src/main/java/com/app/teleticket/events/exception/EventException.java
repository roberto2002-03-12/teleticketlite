package com.app.teleticket.events.exception;

public class EventException extends RuntimeException {

    private final int status;

    public EventException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}