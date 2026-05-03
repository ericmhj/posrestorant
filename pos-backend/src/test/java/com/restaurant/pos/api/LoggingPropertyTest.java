package com.restaurant.pos.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Property 27: All log entries for a request share the same traceId.
 * Verified by checking the X-Trace-Id response header is present and consistent.
 */
@QuarkusTest
class LoggingPropertyTest {

    @Test
    @Tag("Feature: devcontainer-setup, Property 27: Logs contienen traceId consistente")
    void property27_responseContainsTraceIdHeader() {
        // Every response must include X-Trace-Id header (set by TraceIdFilter)
        String traceId = given()
            .contentType("application/json")
            .body("{\"username\": \"admin\", \"password\": \"admin123\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .header("X-Trace-Id", notNullValue())
            .extract().header("X-Trace-Id");

        // traceId must be a valid UUID format
        assertTrue(traceId.matches(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
            "X-Trace-Id must be a valid UUID: " + traceId);
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 27: Logs contienen traceId consistente")
    void property27_errorResponseContainsTraceId() {
        // Error responses must also include traceId
        given()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400)
            .header("X-Trace-Id", notNullValue());
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 27: Logs contienen traceId consistente")
    void property27_eachRequestGetsUniqueTraceId() {
        // Two requests must get different traceIds
        String traceId1 = given()
            .contentType("application/json")
            .body("{\"username\": \"admin\", \"password\": \"admin123\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .extract().header("X-Trace-Id");

        String traceId2 = given()
            .contentType("application/json")
            .body("{\"username\": \"admin\", \"password\": \"admin123\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .extract().header("X-Trace-Id");

        assertNotEquals(traceId1, traceId2,
            "Each request must get a unique traceId");
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private void assertNotEquals(String a, String b, String message) {
        if (a != null && a.equals(b)) throw new AssertionError(message);
    }
}
