package com.hazard.dto.common;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standardized API success response wrapper for non-GeoJSON payload responses.
 *
 * @param <T> Response payload type
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private Map<String, Object> meta = new LinkedHashMap<>();

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }

    public ApiResponse(T data) {
        this.timestamp = LocalDateTime.now();
        this.success = true;
        this.message = "Operation completed successfully";
        this.data = data;
    }

    public ApiResponse(T data, String message) {
        this.timestamp = LocalDateTime.now();
        this.success = true;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(data, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta != null ? meta : new LinkedHashMap<>();
    }

    public ApiResponse<T> addMeta(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }
}
