package com.pk.app.auth.dto.response;

/**
 * Session opened after successful OTP verification.
 *
 * @param partnerUserId platform-wide user id exposed to partners
 * @param accessToken   short-lived JWT; send as Authorization: Bearer
 * @param refreshToken  long-lived token for /auth/refresh when access token expires
 * @param tokenType     token type for Authorization header, usually Bearer
 * @param expiresIn     access token lifetime in seconds
 * @param authAction    REGISTER for first-time mobile; LOGIN for returning user
 * @param newUser       true when the mobile was registered in this request
 * @param passwordSet   true when the user has already set a login password
 * @param userStage     home routing stage; same as {@code GET /home/summary}
 */
public record OtpVerifyResponse(
        String partnerUserId,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        String authAction,
        boolean newUser,
        boolean passwordSet,
        String userStage
) {
}
