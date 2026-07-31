package com.pk.app.repay.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RepayTrialBatchRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotEmpty @Size(max = 20) @Valid List<RepayTrialBatchOrderRequest> repayOrders
) {
}
