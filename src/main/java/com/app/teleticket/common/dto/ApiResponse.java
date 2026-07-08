package com.app.teleticket.common.dto;

public class ApiResponse<T> {

    private String status;
    private int code;
    private ErrorPayload error;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(String status, int code, ErrorPayload error, T data) {
        this.status = status;
        this.code = code;
        this.error = error;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(int code, T data) {
        return new ApiResponse<>("success", code, null, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return success(200, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return success(201, data);
    }

    public static <T> ApiResponse<T> error(int code, String message, String stack) {
        return new ApiResponse<>("error", code, new ErrorPayload(message, stack), null);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ErrorPayload getError() {
        return error;
    }

    public void setError(ErrorPayload error) {
        this.error = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static class ErrorPayload {
        private String message;
        private String stack;

        public ErrorPayload() {
        }

        public ErrorPayload(String message, String stack) {
            this.message = message;
            this.stack = stack;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getStack() {
            return stack;
        }

        public void setStack(String stack) {
            this.stack = stack;
        }
    }
}