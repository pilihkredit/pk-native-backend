package com.pk.app.repay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RepayTrialBatchOrderRequest(
        @NotBlank @Size(max = 64) String loanApplyId,
        @NotNull Boolean settle,
        List<Integer> termNos
) {
}
