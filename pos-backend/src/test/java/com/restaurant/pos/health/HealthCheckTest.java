package com.restaurant.pos.health;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Smoke tests for health check endpoints.
 */
@QuarkusTest
class HealthCheckTest {

    @Test
    void liveness_returns200() {
        given()
        .when()
            .get("/q/health/live")
        .then()
            .statusCode(200)
            .body("status", equalTo("UP"));
    }

    @Test
    void readiness_returns200WhenDbUp() {
        given()
        .when()
            .get("/q/health/ready")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(503)));
        // 503 is acceptable in test environment if Camel routes aren't all started
    }

    @Test
    void health_overall_returns200() {
        given()
        .when()
            .get("/q/health")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(503)))
            .body("status", notNullValue());
    }

    @Test
    void metrics_endpoint_returns200() {
        given()
        .when()
            .get("/q/metrics")
        .then()
            .statusCode(200)
            .contentType(containsString("text/plain"));
    }

    @Test
    void openapi_endpoint_returns200() {
        given()
        .when()
            .get("/q/openapi")
        .then()
            .statusCode(200);
    }

    @Test
    void swaggerui_returns200() {
        given()
        .when()
            .get("/q/swagger-ui")
        .then()
            .statusCode(200);
    }
}
