package com.restaurant.pos.common;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.time.Instant;

/**
 * Catches all exceptions thrown by resource methods and converts them to a
 * consistent {@link ErrorResponse} JSON body.
 *
 * <ul>
 *   <li>{@link ValidationException}  → 400 Bad Request (with field name)</li>
 *   <li>{@link BusinessException}    → HTTP status from the exception</li>
 *   <li>Any other {@link Exception}  → 500 Internal Server Error</li>
 * </ul>
 *
 * The {@code traceId} stored in MDC by {@link TraceIdFilter} is included in
 * every error response so clients can correlate errors with server logs.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(Exception exception) {
        String traceId = getTraceId();
        String timestamp = Instant.now().toString();

        if (exception instanceof ValidationException ve) {
            LOG.warnf("Validation error [traceId=%s] field=%s message=%s",
                    traceId, ve.getField(), ve.getMessage());
            ErrorResponse body = new ErrorResponse(
                    ve.getField(), ve.getMessage(), traceId, timestamp, 400);
            return Response.status(400).entity(body).build();
        }

        if (exception instanceof BusinessException be) {
            LOG.warnf("Business error [traceId=%s] status=%d message=%s",
                    traceId, be.getHttpStatus(), be.getMessage());
            ErrorResponse body = new ErrorResponse(
                    null, be.getMessage(), traceId, timestamp, be.getHttpStatus());
            return Response.status(be.getHttpStatus()).entity(body).build();
        }

        // Unhandled exception → 500
        LOG.errorf(exception, "Unhandled exception [traceId=%s]", traceId);
        ErrorResponse body = new ErrorResponse(
                null, "Error interno del servidor", traceId, timestamp, 500);
        return Response.status(500).entity(body).build();
    }

    private String getTraceId() {
        Object traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
        return traceId != null ? traceId.toString() : "unknown";
    }
}
