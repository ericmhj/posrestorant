package com.restaurant.pos.common;

/**
 * Standard error response body returned by {@link GlobalExceptionMapper}.
 */
public class ErrorResponse {

    public String field;
    public String message;
    public String traceId;
    public String timestamp;
    public int status;

    public ErrorResponse() {
    }

    public ErrorResponse(String field, String message, String traceId, String timestamp, int status) {
        this.field = field;
        this.message = message;
        this.traceId = traceId;
        this.timestamp = timestamp;
        this.status = status;
    }

    // Convenience factory for non-field errors
    public static ErrorResponse of(String message, String traceId, int status) {
        return new ErrorResponse(null, message, traceId,
                java.time.Instant.now().toString(), status);
    }

    // Convenience factory for field-level errors
    public static ErrorResponse ofField(String field, String message, String traceId, int status) {
        return new ErrorResponse(field, message, traceId,
                java.time.Instant.now().toString(), status);
    }
}
