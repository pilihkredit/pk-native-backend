package com.pk.app.repay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RepayTrialRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 64) String loanApplyId,
        List<Integer> termNos,
        String repayType
) {
}
