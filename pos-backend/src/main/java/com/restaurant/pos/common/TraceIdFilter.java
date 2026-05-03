package com.restaurant.pos.common;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;

import java.io.IOException;
import java.util.UUID;

/**
 * JAX-RS filter that generates a unique traceId per request, stores it in MDC
 * so it appears in all log entries, and propagates it to the response via the
 * {@code X-Trace-Id} header.
 */
@Provider
public class TraceIdFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID_KEY, traceId);
        // Store in request context so other components can read it without MDC
        requestContext.setProperty(TRACE_ID_KEY, traceId);
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        Object traceId = requestContext.getProperty(TRACE_ID_KEY);
        if (traceId != null) {
            responseContext.getHeaders().add(TRACE_ID_HEADER, traceId.toString());
        }
        MDC.remove(TRACE_ID_KEY);
    }
}
