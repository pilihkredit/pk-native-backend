package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for sending an OTP.
 *
 * @param mobileNo user mobile number
 * @param deviceNo stable device identifier; must match X-Device-No header when the header is sent
 */
public record OtpSendRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @NotBlank @Size(max = 128) String deviceNo
) {
}
