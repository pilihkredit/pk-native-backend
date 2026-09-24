package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for password login.
 *
 * @param mobileNo registered mobile number
 * @param password login password
 * @param deviceNo device identifier; must match X-Device-No header when the header is sent
 */
public record PasswordLoginRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @NotBlank String password,
        @NotBlank @Size(max = 128) String deviceNo,
        @Size(max = 64) String faceVerifyToken
) {
}
