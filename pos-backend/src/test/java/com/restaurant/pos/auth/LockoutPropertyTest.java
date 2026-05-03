package com.restaurant.pos.auth;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.usuario.Rol;
import com.restaurant.pos.usuario.Usuario;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.quicktheories.QuickTheory.qt;
import static org.quicktheories.generators.SourceDSL.integers;

/**
 * Property-based tests for account lockout after consecutive failed login attempts.
 *
 * <p><b>Validates: Requirements 10.1</b> — the system locks accounts after 5 consecutive
 * failed login attempts to prevent brute-force attacks.
 *
 * @tag Feature: devcontainer-setup, Property 22: Bloqueo de cuenta despues de 5 intentos fallidos
 */
@QuarkusTest
@Tag("Feature: devcontainer-setup, Property 22: Bloqueo de cuenta despues de 5 intentos fallidos")
class LockoutPropertyTest {

    @Inject
    AuthService authService;

    @Inject
    PasswordHasher passwordHasher;

    private static final String CORRECT_PASSWORD = "CorrectPass123!";
    private static final String WRONG_PASSWORD = "WrongPassword!";
    private static final int LOCKOUT_THRESHOLD = 5;

    @BeforeEach
    @Transactional
    void setUp() {
        Usuario.find("username like ?1", "lockout_test_%").stream()
                .forEach(u -> ((Usuario) u).delete());
    }

    /**
     * Property: for any user, after exactly 5 consecutive failed login attempts,
     * the account is locked and subsequent attempts return 401 with a lockout message.
     *
     * <p><b>Validates: Requirements 10.1</b>
     */
    @Test
    void property_accountLockedAfterExactly5FailedAttempts() {
        qt()
                .withExamples(5)
                .forAll(integers().between(1, 5))
                .checkAssert(iteration -> {
                    String username = "lockout_test_" + UUID.randomUUID().toString().substring(0, 8);
                    createUser(username);

                    AuthLoginRequest badRequest = new AuthLoginRequest(username, WRONG_PASSWORD);

                    // Attempts 1-4: should fail but NOT lock the account
                    for (int i = 1; i < LOCKOUT_THRESHOLD; i++) {
                        assertThrows(BusinessException.class,
                                () -> authService.login(badRequest, "127.0.0.1"),
                                "Attempt " + i + " should throw BusinessException");

                        Usuario usuario = Usuario.findByUsername(username).orElseThrow();
                        assertNull(usuario.bloqueadoHasta,
                                "Account should NOT be locked after " + i + " failed attempts");
                    }

                    // Attempt 5: should lock the account
                    assertThrows(BusinessException.class,
                            () -> authService.login(badRequest, "127.0.0.1"),
                            "5th attempt should throw BusinessException");

                    Usuario usuario = Usuario.findByUsername(username).orElseThrow();
                    assertNotNull(usuario.bloqueadoHasta,
                            "Account SHOULD be locked after exactly 5 failed attempts");
                    assertTrue(usuario.bloqueadoHasta.isAfter(java.time.LocalDateTime.now()),
                            "Lock expiry should be in the future");
                });
    }

    /**
     * Property: after account lockout, even correct credentials are rejected with HTTP 401.
     *
     * <p><b>Validates: Requirements 10.1</b>
     */
    @Test
    void property_lockedAccountRejectsCorrectCredentials() {
        qt()
                .withExamples(3)
                .forAll(integers().between(1, 3))
                .checkAssert(iteration -> {
                    String username = "lockout_test_correct_" + UUID.randomUUID().toString().substring(0, 6);
                    createUser(username);

                    // Lock the account
                    AuthLoginRequest badRequest = new AuthLoginRequest(username, WRONG_PASSWORD);
                    for (int i = 0; i < LOCKOUT_THRESHOLD; i++) {
                        assertThrows(BusinessException.class,
                                () -> authService.login(badRequest, "127.0.0.1"));
                    }

                    // Correct credentials should still be rejected
                    AuthLoginRequest goodRequest = new AuthLoginRequest(username, CORRECT_PASSWORD);
                    BusinessException ex = assertThrows(BusinessException.class,
                            () -> authService.login(goodRequest, "127.0.0.1"),
                            "Correct credentials should be rejected when account is locked");

                    assertEquals(401, ex.getHttpStatus());
                    assertTrue(ex.getMessage().contains("bloqueada"),
                            "Error message should indicate account is locked");
                });
    }

    /**
     * Property: failed attempt counter resets to 0 after a successful login.
     *
     * <p><b>Validates: Requirements 10.1</b>
     */
    @Test
    void property_failedCounterResetsAfterSuccessfulLogin() {
        qt()
                .withExamples(3)
                .forAll(integers().between(1, 4))
                .checkAssert(failedAttempts -> {
                    String username = "lockout_test_reset_" + UUID.randomUUID().toString().substring(0, 6);
                    createUser(username);

                    // Make some failed attempts (less than lockout threshold)
                    AuthLoginRequest badRequest = new AuthLoginRequest(username, WRONG_PASSWORD);
                    for (int i = 0; i < failedAttempts; i++) {
                        assertThrows(BusinessException.class,
                                () -> authService.login(badRequest, "127.0.0.1"));
                    }

                    // Verify counter was incremented
                    Usuario usuarioBefore = Usuario.findByUsername(username).orElseThrow();
                    assertEquals(failedAttempts, usuarioBefore.intentosFallidos,
                            "Failed counter should be " + failedAttempts);

                    // Successful login should reset the counter
                    AuthLoginRequest goodRequest = new AuthLoginRequest(username, CORRECT_PASSWORD);
                    authService.login(goodRequest, "127.0.0.1");

                    Usuario usuarioAfter = Usuario.findByUsername(username).orElseThrow();
                    assertEquals(0, usuarioAfter.intentosFallidos,
                            "Failed counter should be reset to 0 after successful login");
                    assertNull(usuarioAfter.bloqueadoHasta,
                            "bloqueadoHasta should be null after successful login");
                });
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    @Transactional
    void createUser(String username) {
        Usuario usuario = new Usuario();
        usuario.nombre = "Lockout";
        usuario.apellido = "Test";
        usuario.username = username;
        usuario.passwordHash = passwordHasher.hash(CORRECT_PASSWORD);
        usuario.rol = Rol.MESERO;
        usuario.activo = true;
        usuario.intentosFallidos = 0;
        usuario.persist();
    }
}
