package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for checking whether a mobile number is already registered.
 *
 * @param mobileNo user mobile number
 * @param deviceNo stable device identifier; must match X-Device-No header when the header is sent
 */
public record MobileCheckRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @NotBlank @Size(max = 128) String deviceNo
) {
}
