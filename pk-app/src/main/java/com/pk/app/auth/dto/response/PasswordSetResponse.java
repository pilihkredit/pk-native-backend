package com.pk.app.auth.dto.response;

/**
 * Password setup result.
 *
 * @param passwordSet always true after successful setup
 */
public record PasswordSetResponse(boolean passwordSet) {
}
