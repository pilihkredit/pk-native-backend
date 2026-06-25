package com.pk.app.credit.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreditApplyRequest(
        @NotBlank @Size(max = 64) String requestId,
        BigDecimal lat,
        BigDecimal lng,
        @Size(max = 32) String ip,
        @Size(max = 512) String address,
        @NotNull @Valid CreditRiskDataInfoRequest riskDataInfo
) {
}
