package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Personal profile fields aligned with lender OpenAPI {@code userInfo.profile} (v1.1.0+).
 */
public record ProfilePersonalInfoRequest(
        @NotNull Integer educationDegree,
        @NotNull Integer industry,
        @NotBlank
        @Pattern(regexp = "\\d{5,9}")
        @Size(max = 16)
        String income,
        @NotBlank @Size(max = 128) String motherSurname,
        @Size(max = 128) String userEmail
) {
}
