package com.restaurant.pos.common;

/**
 * Exception thrown when a request field fails domain-level validation
 * (beyond Bean Validation constraints).
 */
public class ValidationException extends RuntimeException {

    private final String field;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
