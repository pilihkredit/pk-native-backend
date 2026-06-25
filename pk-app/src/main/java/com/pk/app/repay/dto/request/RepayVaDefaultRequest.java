package com.pk.app.repay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RepayVaDefaultRequest(
        @NotBlank @Size(max = 64) String vaNo,
        @NotBlank @Size(max = 64) String bankChannel
) {
}
