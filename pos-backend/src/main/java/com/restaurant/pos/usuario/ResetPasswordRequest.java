package com.restaurant.pos.usuario;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for resetting a user's password (Admin operation).
 *
 * @param newPassword the new plaintext password to set
 */
public record ResetPasswordRequest(
        @NotBlank String newPassword
) {
}
