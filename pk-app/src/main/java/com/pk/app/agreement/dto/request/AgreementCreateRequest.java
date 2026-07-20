package com.pk.app.agreement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AgreementCreateRequest(
        @Size(max = 64) String partnerUserId,
        @NotBlank @Size(max = 128) String deviceNo,
        @NotNull @Positive Long clickedAt,
        @NotEmpty @Size(max = 50) @Valid List<AgreementItemRequest> items
) {
}
