package com.pk.app.auth.dto.response;

/**
 * Mobile registration status for pre-OTP UI routing.
 *
 * @param registered    true when the mobile number already has a user profile
 * @param accountStatus EXISTING when registered, NEW otherwise
 * @param passwordSet   true when the user has set a login password; false for new or OTP-only users
 */
public record MobileCheckResponse(boolean registered, String accountStatus, boolean passwordSet) {
}
