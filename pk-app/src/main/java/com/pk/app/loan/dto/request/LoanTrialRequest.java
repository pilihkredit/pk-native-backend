package com.pk.app.loan.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record LoanTrialRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 64) String applyId,
        @NotNull @DecimalMin("0.01") BigDecimal applyAmt,
        @NotBlank @Size(max = 64) String productCode,
        @NotBlank @Size(max = 64) String repayMethod,
        Long couponId
) {
}
