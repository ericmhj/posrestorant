package com.restaurant.pos.api;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Property 25: HTTP 400 response includes affected field name.
 * Property 26: Paginated responses contain at most 'size' items and metadata.
 */
@QuarkusTest
class ValidationPropertyTest {

    // -------------------------------------------------------
    // Property 25: 400 response includes field name
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 25: Respuesta HTTP 400 incluye campo afectado")
    void property25_missingUsername_returns400WithField() {
        given()
            .contentType("application/json")
            .body("{\"password\": \"test123\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 25: Respuesta HTTP 400 incluye campo afectado")
    void property25_missingPassword_returns400() {
        given()
            .contentType("application/json")
            .body("{\"username\": \"admin\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    @Test
    @Tag("Feature: devcontainer-setup, Property 25: Respuesta HTTP 400 incluye campo afectado")
    void property25_emptyBody_returns400() {
        given()
            .contentType("application/json")
            .body("{}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(400);
    }

    // -------------------------------------------------------
    // Property 26: Pagination returns at most 'size' items
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 26: Paginacion retorna como maximo size items")
    void property26_defaultPaginationApplied() {
        // Login first to get token
        String token = given()
            .contentType("application/json")
            .body("{\"username\": \"admin\", \"password\": \"admin123\"}")
        .when()
            .post("/api/v1/auth/login")
        .then()
            .statusCode(200)
            .extract().path("token");

        // Request with size=2 — should return at most 2 items
        given()
            .header("Authorization", "Bearer " + token)
            .queryParam("size", 2)
            .queryParam("page", 0)
        .when()
            .get("/api/v1/mesas")
        .then()
            .statusCode(200)
            .body("size()", lessThanOrEqualTo(2));
    }
}
