package com.restaurant.pos.common;

/**
 * Generic success response wrapper used by resource endpoints.
 *
 * @param <T> the type of the response payload
 */
public class ApiResponse<T> {

    public T data;
    public String message;
    public String traceId;

    public ApiResponse() {
    }

    public ApiResponse(T data, String message, String traceId) {
        this.data = data;
        this.message = message;
        this.traceId = traceId;
    }

    public static <T> ApiResponse<T> of(T data, String traceId) {
        return new ApiResponse<>(data, null, traceId);
    }

    public static <T> ApiResponse<T> of(T data, String message, String traceId) {
        return new ApiResponse<>(data, message, traceId);
    }
}
