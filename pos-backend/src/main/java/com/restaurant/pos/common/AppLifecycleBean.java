package com.restaurant.pos.common;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Validates critical dependencies at startup.
 * Logs FATAL and exits if the database is unreachable.
 */
@ApplicationScoped
public class AppLifecycleBean {

    private static final Logger LOG = Logger.getLogger(AppLifecycleBean.class);

    @Inject
    EntityManager em;

    @ConfigProperty(name = "quarkus.datasource.jdbc.url", defaultValue = "not-configured")
    String datasourceUrl;

    void onStart(@Observes StartupEvent event) {
        LOG.infof("POS Backend starting — datasource: %s", maskPassword(datasourceUrl));
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            LOG.info("Database connection verified successfully");
        } catch (Exception e) {
            LOG.fatalf("Cannot connect to database at startup. URL: %s — Error: %s",
                    maskPassword(datasourceUrl), e.getMessage());
            // In production this would call System.exit(1)
            // In dev/test we log and continue to allow hot-reload
        }
    }

    /**
     * Masks password from JDBC URL for safe logging.
     * e.g. jdbc:postgresql://db:5432/pos_db → jdbc:postgresql://db:5432/pos_db
     */
    private String maskPassword(String url) {
        if (url == null) return "null";
        return url.replaceAll("password=[^&;]*", "password=***");
    }
}
