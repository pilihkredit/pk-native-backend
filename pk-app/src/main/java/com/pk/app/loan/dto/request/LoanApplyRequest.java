package com.pk.app.loan.dto.request;

import com.pk.app.credit.dto.request.CreditRiskDataInfoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record LoanApplyRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 64) String applyId,
        @NotBlank @Size(max = 64) String quoteNo,
        @Size(max = 256) String loanPurpose,
        BigDecimal lat,
        BigDecimal lng,
        @Size(max = 32) String ip,
        @NotNull @Valid CreditRiskDataInfoRequest riskDataInfo
) {
}
