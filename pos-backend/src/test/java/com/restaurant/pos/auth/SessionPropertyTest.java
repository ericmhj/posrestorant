package com.restaurant.pos.auth;

import com.restaurant.pos.usuario.Rol;
import com.restaurant.pos.usuario.Usuario;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;

/**
 * Property-based tests for session invalidation after logout.
 *
 * <p><b>Validates: Requirements 10.9</b> — after logout, the session token is invalidated
 * and cannot be reused.
 *
 * @tag Feature: devcontainer-setup, Property 4: Sesion invalida despues de logout
 */
@QuarkusTest
@Tag("Feature: devcontainer-setup, Property 4: Sesion invalida despues de logout")
class SessionPropertyTest {

    @Inject
    AuthService authService;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    SessionStore sessionStore;

    private static final String PASSWORD = "SessionTest123!";

    @BeforeEach
    @Transactional
    void setUp() {
        Usuario.find("username like ?1", "session_test_%").stream()
                .forEach(u -> ((Usuario) u).delete());
    }

    /**
     * Property: for any valid token T, after logout with T, the token is no longer valid
     * in the SessionStore.
     *
     * <p><b>Validates: Requirements 10.9</b>
     */
    @Test
    void property_tokenIsInvalidAfterLogout() {
        qt()
                .withExamples(10)
                .forAll(integers().between(1, 10))
                .checkAssert(iteration -> {
                    String username = "session_test_" + UUID.randomUUID().toString().substring(0, 8);
                    String token = createUserAndLogin(username);

                    // Token should be valid before logout
                    assertTrue(sessionStore.isValid(token),
                            "Token should be valid before logout (iteration " + iteration + ")");

                    // Logout
                    authService.logout(token);

                    // Token should be invalid after logout
                    assertFalse(sessionStore.isValid(token),
                            "Token should be invalid after logout (iteration " + iteration + ")");
                });
    }

    /**
     * Property: after logout, any HTTP request using the invalidated token returns 401.
     *
     * <p><b>Validates: Requirements 10.9</b>
     */
    @Test
    void property_requestWithInvalidatedTokenReturns401() {
        qt()
                .withExamples(5)
                .forAll(integers().between(1, 5))
                .checkAssert(iteration -> {
                    String username = "session_test_req_" + UUID.randomUUID().toString().substring(0, 6);
                    String token = createUserAndLogin(username);

                    // Verify token works before logout
                    int statusBefore = given()
                            .header("Authorization", "Bearer " + token)
                            .when()
                            .post("/api/v1/auth/logout")
                            .then()
                            .extract()
                            .statusCode();

                    // After logout, the same token should return 401
                    int statusAfter = given()
                            .header("Authorization", "Bearer " + token)
                            .when()
                            .get("/api/v1/usuarios")
                            .then()
                            .extract()
                            .statusCode();

                    assert statusAfter == 401
                            : "Request with invalidated token should return 401, got: " + statusAfter;
                });
    }

    /**
     * Property: multiple sessions for the same user are all invalidated when the user is deactivated.
     *
     * <p><b>Validates: Requirements 10.9</b>
     */
    @Test
    void property_allSessionsInvalidatedOnUserDeactivation() {
        qt()
                .withExamples(3)
                .forAll(integers().between(2, 4))
                .checkAssert(sessionCount -> {
                    String username = "session_test_multi_" + UUID.randomUUID().toString().substring(0, 6);
                    String[] tokens = new String[sessionCount];

                    // Create multiple sessions for the same user
                    for (int i = 0; i < sessionCount; i++) {
                        tokens[i] = createUserAndLogin(username);
                    }

                    // All tokens should be valid
                    for (String token : tokens) {
                        assertTrue(sessionStore.isValid(token),
                                "All tokens should be valid before deactivation");
                    }

                    // Deactivate user (invalidates all sessions)
                    Usuario usuario = Usuario.findByUsername(username).orElseThrow();
                    sessionStore.invalidateAllForUser(usuario.id);

                    // All tokens should now be invalid
                    for (String token : tokens) {
                        assertFalse(sessionStore.isValid(token),
                                "All tokens should be invalid after user deactivation");
                    }
                });
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    @Transactional
    String createUserAndLogin(String username) {
        // Check if user already exists (for multi-session test)
        if (Usuario.findByUsername(username).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.nombre = "Session";
            usuario.apellido = "Test";
            usuario.username = username;
            usuario.passwordHash = passwordHasher.hash(PASSWORD);
            usuario.rol = Rol.MESERO;
            usuario.activo = true;
            usuario.intentosFallidos = 0;
            usuario.persist();
        }

        AuthLoginRequest loginRequest = new AuthLoginRequest(username, PASSWORD);
        AuthLoginResponse response = authService.login(loginRequest, "127.0.0.1");
        return response.token();
    }
}
