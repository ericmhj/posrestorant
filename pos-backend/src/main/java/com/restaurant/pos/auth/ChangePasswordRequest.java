package com.restaurant.pos.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for the {@code PUT /api/v1/auth/password} endpoint.
 *
 * @param currentPassword the user's current plaintext password (for verification)
 * @param newPassword     the desired new plaintext password
 */
public record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank String newPassword
) {
}
