package com.pk.app.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OtpVerifyRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @NotBlank @Size(max = 128) String otpToken,
        @NotBlank @Size(min = 4, max = 8) String otpCode,
        @NotBlank @Size(max = 128) String deviceNo,
        @Size(max = 32) String loginIp,
        BigDecimal loginLat,
        BigDecimal loginLng
) {
}
