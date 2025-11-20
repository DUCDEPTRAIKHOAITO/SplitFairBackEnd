package com.anygroup.splitfair.dto;

import java.time.LocalDateTime;

public class APIResponse<T> {
    private String status;       // Trạng thái thành công hoặc lỗi
    private String message;      // Thông điệp phản hồi
    private T data;              // Dữ liệu trả về
    private String error;        // Lỗi chi tiết (nếu có)
    private LocalDateTime timestamp; // Thời gian trả về phản hồi

    // Constructor
    public APIResponse(String status, String message, T data, String error, LocalDateTime timestamp) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.error = error;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
