package com.restaurant.pos.camel;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.Exchange;
import org.jboss.logging.Logger;

import java.time.Instant;

@ApplicationScoped
public class DeadLetterService {

    private static final Logger LOG = Logger.getLogger(DeadLetterService.class);

    /**
     * Persists a failed message to the log.
     * In production this could write to a dead_letter_messages table.
     */
    public void persist(Exchange exchange) {
        String routeId = exchange.getFromRouteId();
        Object body = exchange.getIn().getBody();
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        int retries = exchange.getProperty(Exchange.REDELIVERY_COUNTER, 0, Integer.class);

        LOG.errorf("Dead Letter Channel: routeId=%s retries=%d timestamp=%s cause=%s body=%s",
                routeId,
                retries,
                Instant.now(),
                cause != null ? cause.getMessage() : "unknown",
                body != null ? body.toString() : "null");
    }
}
