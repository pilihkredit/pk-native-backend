package com.pk.app.loan.dto.request;

import com.pk.app.credit.dto.request.CreditRiskDataInfoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Loan apply request. {@code applyAmt}/{@code productCode}/{@code repayMethod}/{@code couponId}
 * may be omitted; the server fills them from the trial snapshot for {@code quoteNo}
 * (matches published BFF contract). When provided, they override the snapshot values.
 */
public record LoanApplyRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 64) String applyId,
        @NotBlank @Size(max = 64) String quoteNo,
        BigDecimal applyAmt,
        @Size(max = 64) String productCode,
        @Size(max = 64) String repayMethod,
        Long couponId,
        @Size(max = 256) String loanPurpose,
        BigDecimal lat,
        BigDecimal lng,
        @Size(max = 32) String ip,
        @Size(max = 512) String address,
        @NotNull @Valid CreditRiskDataInfoRequest riskDataInfo
) {
}
