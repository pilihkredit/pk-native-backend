package com.pk.app.auth.dto.response;

/**
 * Mobile registration status for pre-OTP UI routing.
 *
 * @param registered    true when the mobile number already has a user profile
 * @param accountStatus EXISTING when registered, NEW otherwise
 */
public record MobileCheckResponse(boolean registered, String accountStatus) {
}
