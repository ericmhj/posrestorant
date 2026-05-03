package com.restaurant.pos.health;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

/**
 * Exposes build and version information at /q/info.
 * Quarkus also provides this automatically via quarkus-info extension,
 * but this provides a simple fallback.
 */
@Path("/q/app-info")
@Produces(MediaType.APPLICATION_JSON)
public class AppInfoResource {

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0-SNAPSHOT")
    String version;

    @ConfigProperty(name = "quarkus.application.name", defaultValue = "pos-backend")
    String appName;

    @GET
    public Map<String, String> info() {
        return Map.of(
                "name", appName,
                "version", version,
                "status", "running"
        );
    }
}
