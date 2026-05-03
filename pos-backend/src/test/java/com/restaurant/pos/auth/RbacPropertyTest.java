package com.restaurant.pos.auth;

import com.restaurant.pos.usuario.Rol;
import com.restaurant.pos.usuario.Usuario;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.arbitrary;

/**
 * Property-based tests for RBAC enforcement.
 *
 * <p><b>Validates: Requirements 10.4–10.8</b> — users can only perform actions
 * permitted by their role; unauthorized actions return HTTP 403.
 *
 * @tag Feature: devcontainer-setup, Property 3: RBAC - acciones fuera de rol son rechazadas
 */
@QuarkusTest
@Tag("Feature: devcontainer-setup, Property 3: RBAC - acciones fuera de rol son rechazadas")
class RbacPropertyTest {

    @Inject
    AuthService authService;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    SessionStore sessionStore;

    private static final String PASSWORD = "RbacTest123!";

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any leftover test users
        Usuario.find("username like ?1", "rbac_test_%").stream()
                .forEach(u -> ((Usuario) u).delete());
    }

    /**
     * Property: for any endpoint that requires ADMIN role, a user with a non-ADMIN role
     * receives HTTP 403.
     *
     * <p><b>Validates: Requirements 10.4–10.8</b>
     */
    @Test
    void property_nonAdminUserReceives403OnAdminEndpoints() {
        // Non-admin roles
        List<Rol> nonAdminRoles = Arrays.asList(Rol.MESERO, Rol.COCINA, Rol.BARRA);

        qt()
                .withExamples(nonAdminRoles.size())
                .forAll(arbitrary().pick(nonAdminRoles))
                .checkAssert(rol -> {
                    String username = "rbac_test_" + rol.name().toLowerCase() + "_" +
                            UUID.randomUUID().toString().substring(0, 6);
                    String token = createUserAndLogin(username, rol);

                    // Admin-only endpoint: GET /api/v1/usuarios
                    int status = given()
                            .header("Authorization", "Bearer " + token)
                            .when()
                            .get("/api/v1/usuarios")
                            .then()
                            .extract()
                            .statusCode();

                    assertEquals(403, status,
                            "Role " + rol + " should receive 403 on admin-only endpoint");
                });
    }

    /**
     * Property: ADMIN role can access all admin endpoints (returns 200, not 403).
     *
     * <p><b>Validates: Requirements 10.7</b>
     */
    @Test
    void property_adminUserCanAccessAdminEndpoints() {
        String username = "rbac_test_admin_" + UUID.randomUUID().toString().substring(0, 6);
        String token = createUserAndLogin(username, Rol.ADMIN);

        int status = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/usuarios")
                .then()
                .extract()
                .statusCode();

        assertEquals(200, status, "ADMIN should be able to access admin endpoints");
    }

    /**
     * Property: any request without a valid token receives HTTP 401.
     *
     * <p><b>Validates: Requirements 10.1</b>
     */
    @Test
    void property_requestWithoutTokenReceives401() {
        // Endpoints that require authentication
        List<String> protectedEndpoints = Arrays.asList(
                "/api/v1/usuarios",
                "/api/v1/auth/logout"
        );

        qt()
                .withExamples(protectedEndpoints.size())
                .forAll(arbitrary().pick(protectedEndpoints))
                .checkAssert(endpoint -> {
                    int status = given()
                            .when()
                            .get(endpoint)
                            .then()
                            .extract()
                            .statusCode();

                    assertEquals(401, status,
                            "Endpoint " + endpoint + " should return 401 without token");
                });
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    @Transactional
    String createUserAndLogin(String username, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.nombre = "Test";
        usuario.apellido = "User";
        usuario.username = username;
        usuario.passwordHash = passwordHasher.hash(PASSWORD);
        usuario.rol = rol;
        usuario.activo = true;
        usuario.intentosFallidos = 0;
        usuario.persist();

        AuthLoginRequest loginRequest = new AuthLoginRequest(username, PASSWORD);
        AuthLoginResponse response = authService.login(loginRequest, "127.0.0.1");
        return response.token();
    }
}
