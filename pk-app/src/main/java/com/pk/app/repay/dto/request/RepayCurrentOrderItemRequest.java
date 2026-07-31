package com.pk.app.repay.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RepayCurrentOrderItemRequest(
        @NotBlank @Size(max = 64) String loanApplyId,
        @NotEmpty List<Integer> termNos
) {
}
