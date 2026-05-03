package com.restaurant.pos.common;

/**
 * Base exception for domain/business rule violations.
 * Carries an HTTP status code so the exception mapper can produce the correct response.
 */
public class BusinessException extends RuntimeException {

    private final int httpStatus;

    public BusinessException(int httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    // -------------------------------------------------------
    // Concrete subclasses
    // -------------------------------------------------------

    /** 409 Conflict – mesa already has an open cuenta. */
    public static class MesaOcupadaException extends BusinessException {
        public MesaOcupadaException(String message) {
            super(409, message);
        }
    }

    /** 400 Bad Request – not enough stock to fulfil the reservation. */
    public static class StockInsuficienteException extends BusinessException {
        public StockInsuficienteException(String message) {
            super(400, message);
        }
    }

    /** 404 Not Found – requested resource does not exist. */
    public static class RecursoNoEncontradoException extends BusinessException {
        public RecursoNoEncontradoException(String message) {
            super(404, message);
        }
    }

    /** 409 Conflict – entity is in a state that prevents the requested operation. */
    public static class ConflictoEstadoException extends BusinessException {
        public ConflictoEstadoException(String message) {
            super(409, message);
        }
    }
}
