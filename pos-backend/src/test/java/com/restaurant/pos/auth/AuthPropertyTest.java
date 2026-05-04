package com.restaurant.pos.auth;

import com.restaurant.pos.common.BusinessException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property 1: Authentication rejects invalid credentials.
 * Property 2: Password hash is irreversible.
 */
@QuarkusTest
class AuthPropertyTest {

    @Inject
    AuthService authService;

    @Inject
    PasswordHasher passwordHasher;

    // -------------------------------------------------------
    // Property 1: Invalid credentials always return 401
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 1: Autenticacion rechaza credenciales invalidas")
    void property1_invalidCredentials_return401() {
        // 20 combinations of non-existent usernames and wrong passwords
        String[][] cases = {
            {"nonexistent", "password"}, {"admin", "wrongpass"}, {"", ""},
            {"hacker", "hack123"}, {"admin", ""}, {"", "admin123"},
            {"user1", "pass1"}, {"user2", "pass2"}, {"test", "test"},
            {"root", "root"}, {"admin2", "admin123"}, {"mesero1", "wrong"},
            {"cocina1", "wrong"}, {"barra1", "wrong"}, {"x", "y"},
            {"admin", "Admin123"}, {"ADMIN", "admin123"}, {"admin ", "admin123"},
            {"admin", "admin124"}, {"admin", "admin12"}
        };

        for (String[] c : cases) {
            String username = c[0];
            String password = c[1];

            // Skip the valid admin credential
            if ("admin".equals(username) && "admin123".equals(password)) continue;

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> authService.login(new AuthLoginRequest(username, password), "127.0.0.1"),
                    "Should reject: username=" + username);
            assertEquals(401, ex.getHttpStatus(),
                    "Should return 401 for invalid credentials: " + username);
        }
    }

    // -------------------------------------------------------
    // Property 2: BCrypt hash is irreversible
    // -------------------------------------------------------
    @Test
    @Tag("Feature: devcontainer-setup, Property 2: Hash de contrasena es irreversible")
    void property2_passwordHashIsIrreversible() {
        String[] passwords = {
            "password123", "admin", "secret", "P@ssw0rd!", "12345678",
            "qwerty", "letmein", "monkey", "dragon", "master",
            "abc123", "pass", "test", "hello", "welcome",
            "login", "admin123", "root", "toor", "changeme"
        };

        for (String plaintext : passwords) {
            String hash = passwordHasher.hash(plaintext);

            // Hash must not equal plaintext
            assertNotEquals(plaintext, hash,
                    "Hash must not equal plaintext for: " + plaintext);

            // Hash must start with bcrypt prefix
            assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"),
                    "Hash must be bcrypt format for: " + plaintext);

            // verify must return true for correct password
            assertTrue(passwordHasher.verify(plaintext, hash),
                    "verify(plaintext, hash) must return true for: " + plaintext);

            // verify must return false for wrong password
            assertFalse(passwordHasher.verify(plaintext + "x", hash),
                    "verify must return false for wrong password: " + plaintext);
        }
    }
}
