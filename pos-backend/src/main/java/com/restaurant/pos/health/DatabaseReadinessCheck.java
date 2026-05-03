package com.restaurant.pos.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.apache.camel.CamelContext;
import org.apache.camel.ServiceStatus;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Readiness;

@Readiness
@ApplicationScoped
public class DatabaseReadinessCheck implements HealthCheck {

    @Inject
    EntityManager em;

    @Inject
    CamelContext camelContext;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponseBuilder builder = HealthCheckResponse.builder()
                .name("database-readiness");

        // Check DB connectivity
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            builder.withData("database", "UP");
        } catch (Exception e) {
            return builder.down()
                    .withData("database", "DOWN")
                    .withData("error", e.getMessage())
                    .build();
        }

        // Check Camel routes
        boolean allRoutesStarted = camelContext.getRoutes().stream()
                .allMatch(route -> {
                    ServiceStatus status = camelContext.getRouteController()
                            .getRouteStatus(route.getRouteId());
                    return status == ServiceStatus.Started;
                });

        if (!allRoutesStarted) {
            return builder.down()
                    .withData("camelRoutes", "Some routes are not started")
                    .build();
        }

        builder.withData("camelRoutes", "UP");
        return builder.up().build();
    }
}
