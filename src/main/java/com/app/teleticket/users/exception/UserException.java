package com.app.teleticket.users.exception;

public class UserException extends RuntimeException {

    private final int status;

    public UserException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
