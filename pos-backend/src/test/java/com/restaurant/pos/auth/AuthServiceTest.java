package com.restaurant.pos.auth;

import com.restaurant.pos.common.BusinessException;
import com.restaurant.pos.common.ValidationException;
import com.restaurant.pos.usuario.Rol;
import com.restaurant.pos.usuario.Usuario;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link AuthService} using H2 in-memory database.
 */
@QuarkusTest
class AuthServiceTest {

    @Inject
    AuthService authService;

    @Inject
    PasswordHasher passwordHasher;

    @Inject
    SessionStore sessionStore;

    private static final String TEST_USERNAME = "testuser_auth_" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TEST_PASSWORD = "TestPass123!";

    @BeforeEach
    @Transactional
    void setUp() {
        // Remove any existing test user
        Usuario.find("username", TEST_USERNAME).stream().forEach(u -> ((Usuario) u).delete());

        // Create a fresh test user
        Usuario usuario = new Usuario();
        usuario.nombre = "Test";
        usuario.apellido = "User";
        usuario.username = TEST_USERNAME;
        usuario.passwordHash = passwordHasher.hash(TEST_PASSWORD);
        usuario.rol = Rol.MESERO;
        usuario.activo = true;
        usuario.intentosFallidos = 0;
        usuario.persist();
    }

    // -------------------------------------------------------
    // Login tests
    // -------------------------------------------------------

    @Test
    void login_withValidCredentials_returnsTokenAndUserInfo() {
        AuthLoginRequest request = new AuthLoginRequest(TEST_USERNAME, TEST_PASSWORD);

        AuthLoginResponse response = authService.login(request, "127.0.0.1");

        assertNotNull(response);
        assertNotNull(response.token());
        assertFalse(response.token().isBlank());
        assertNotNull(response.usuario());
        assertEquals(TEST_USERNAME, response.usuario().nombre() + " " + response.usuario().apellido()
                // just check the token is valid in session store
                .replace("User", "").trim());
        assertTrue(sessionStore.isValid(response.token()));
    }

    @Test
    void login_withValidCredentials_tokenIsValidInSessionStore() {
        AuthLoginRequest request = new AuthLoginRequest(TEST_USERNAME, TEST_PASSWORD);

        AuthLoginResponse response = authService.login(request, "127.0.0.1");

        assertTrue(sessionStore.isValid(response.token()),
                "Token should be valid in SessionStore after successful login");
    }

    @Test
    void login_withWrongPassword_throwsBusinessException401() {
        AuthLoginRequest request = new AuthLoginRequest(TEST_USERNAME, "WrongPassword!");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(request, "127.0.0.1"));

        assertEquals(401, ex.getHttpStatus());
    }

    @Test
    void login_withWrongPassword_incrementsIntentosFallidos() {
        AuthLoginRequest request = new AuthLoginRequest(TEST_USERNAME, "WrongPassword!");

        assertThrows(BusinessException.class, () -> authService.login(request, "127.0.0.1"));

        Usuario usuario = Usuario.findByUsername(TEST_USERNAME).orElseThrow();
        assertEquals(1, usuario.intentosFallidos);
    }

    @Test
    void login_after5FailedAttempts_locksAccount() {
        AuthLoginRequest badRequest = new AuthLoginRequest(TEST_USERNAME, "WrongPassword!");

        // 5 consecutive failed attempts
        for (int i = 0; i < 5; i++) {
            assertThrows(BusinessException.class, () -> authService.login(badRequest, "127.0.0.1"));
        }

        // Account should now be locked
        Usuario usuario = Usuario.findByUsername(TEST_USERNAME).orElseThrow();
        assertNotNull(usuario.bloqueadoHasta,
                "Account should be locked after 5 failed attempts");
        assertTrue(usuario.bloqueadoHasta.isAfter(java.time.LocalDateTime.now()),
                "Lock expiry should be in the future");
    }

    @Test
    void login_withLockedAccount_throwsBusinessException401() {
        AuthLoginRequest badRequest = new AuthLoginRequest(TEST_USERNAME, "WrongPassword!");

        // Lock the account
        for (int i = 0; i < 5; i++) {
            assertThrows(BusinessException.class, () -> authService.login(badRequest, "127.0.0.1"));
        }

        // Attempt with correct password — should still be rejected
        AuthLoginRequest goodRequest = new AuthLoginRequest(TEST_USERNAME, TEST_PASSWORD);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(goodRequest, "127.0.0.1"));

        assertEquals(401, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("bloqueada"),
                "Error message should mention account is locked");
    }

    @Test
    void login_withUnknownUsername_throwsBusinessException401() {
        AuthLoginRequest request = new AuthLoginRequest("nonexistent_user_xyz", "anyPassword");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> authService.login(request, "127.0.0.1"));

        assertEquals(401, ex.getHttpStatus());
    }

    // -------------------------------------------------------
    // Logout tests
    // -------------------------------------------------------

    @Test
    void logout_invalidatesToken() {
        AuthLoginRequest request = new AuthLoginRequest(TEST_USERNAME, TEST_PASSWORD);
        AuthLoginResponse loginResponse = authService.login(request, "127.0.0.1");
        String token = loginResponse.token();

        assertTrue(sessionStore.isValid(token), "Token should be valid before logout");

        authService.logout(token);

        assertFalse(sessionStore.isValid(token),
                "Token should be invalid after logout");
    }

    // -------------------------------------------------------
    // Change password tests
    // -------------------------------------------------------

    @Test
    void changePassword_withCorrectCurrentPassword_updatesHash() {
        // First login to get userId
        AuthLoginRequest loginRequest = new AuthLoginRequest(TEST_USERNAME, TEST_PASSWORD);
        AuthLoginResponse loginResponse = authService.login(loginRequest, "127.0.0.1");
        UUID userId = loginResponse.usuario().id();

        String newPassword = "NewSecurePass456!";
        ChangePasswordRequest changeRequest = new ChangePasswordRequest(TEST_PASSWORD, newPassword);

        authService.changePassword(userId, changeRequest);

        // Verify new password works
        Usuario usuario = Usuario.findByUsername(TEST_USERNAME).orElseThrow();
        assertTrue(passwordHasher.verify(newPassword, usuario.passwordHash),
                "New password should verify against updated hash");
        assertFalse(passwordHasher.verify(TEST_PASSWORD, usuario.passwordHash),
                "Old password should no longer verify");
    }

    @Test
    void changePassword_withWrongCurrentPassword_throwsValidationException() {
        AuthLoginRequest loginRequest = new AuthLoginRequest(TEST_USERNAME, TEST_PASSWORD);
        AuthLoginResponse loginResponse = authService.login(loginRequest, "127.0.0.1");
        UUID userId = loginResponse.usuario().id();

        ChangePasswordRequest changeRequest = new ChangePasswordRequest("WrongCurrentPass!", "NewPass123!");

        ValidationException ex = assertThrows(ValidationException.class,
                () -> authService.changePassword(userId, changeRequest));

        assertEquals("currentPassword", ex.getField());
    }
}
