package com.restaurant.pos.auth;

import com.restaurant.pos.usuario.Rol;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Generates and validates opaque session tokens.
 *
 * <p>Token format (Base64-encoded): {@code userId:rol:timestamp:random}
 * <ul>
 *   <li>{@code userId}    — UUID of the authenticated user</li>
 *   <li>{@code rol}       — role name (e.g. ADMIN, MESERO)</li>
 *   <li>{@code timestamp} — epoch millis at generation time</li>
 *   <li>{@code random}    — 32 random hex chars for uniqueness</li>
 * </ul>
 *
 * <p>Expiry is enforced by {@link SessionStore}, not by the token itself.
 */
@ApplicationScoped
public class TokenGenerator {

    private static final Logger LOG = Logger.getLogger(TokenGenerator.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a new opaque token for the given user and role.
     *
     * @param userId the authenticated user's UUID
     * @param rol    the user's role
     * @return a Base64-encoded token string
     */
    public String generate(UUID userId, Rol rol) {
        String random = generateRandomHex(16);
        String raw = userId.toString() + ":" + rol.name() + ":" + Instant.now().toEpochMilli() + ":" + random;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates and decodes a token, returning its claims if the format is valid.
     * Note: expiry validation is delegated to {@link SessionStore}.
     *
     * @param token the Base64-encoded token string
     * @return an {@link Optional} containing the {@link TokenClaims} if valid, empty otherwise
     */
    public Optional<TokenClaims> validate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(token);
            String raw = new String(decoded, StandardCharsets.UTF_8);
            String[] parts = raw.split(":", 4);
            if (parts.length != 4) {
                return Optional.empty();
            }
            UUID userId = UUID.fromString(parts[0]);
            Rol rol = Rol.valueOf(parts[1]);
            return Optional.of(new TokenClaims(userId, rol));
        } catch (IllegalArgumentException e) {
            LOG.debugf("Token validation failed: %s", e.getMessage());
            return Optional.empty();
        }
    }

    // -------------------------------------------------------
    // Inner record
    // -------------------------------------------------------

    /**
     * Claims extracted from a validated token.
     *
     * @param userId the user's UUID
     * @param rol    the user's role
     */
    public record TokenClaims(UUID userId, Rol rol) {
    }

    // -------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------

    private String generateRandomHex(int bytes) {
        byte[] buf = new byte[bytes];
        SECURE_RANDOM.nextBytes(buf);
        StringBuilder sb = new StringBuilder(bytes * 2);
        for (byte b : buf) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
