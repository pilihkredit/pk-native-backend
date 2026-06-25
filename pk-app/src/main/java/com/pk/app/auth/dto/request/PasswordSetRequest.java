package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for first-time password setup.
 *
 * @param password        login password; at least 6 characters with letters and digits
 * @param confirmPassword must match password
 */
public record PasswordSetRequest(
        @NotBlank String password,
        @NotBlank String confirmPassword
) {
}
