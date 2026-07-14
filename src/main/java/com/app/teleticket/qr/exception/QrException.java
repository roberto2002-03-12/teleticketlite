package com.app.teleticket.qr.exception;

public class QrException extends RuntimeException {

    private final int status;

    public QrException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}