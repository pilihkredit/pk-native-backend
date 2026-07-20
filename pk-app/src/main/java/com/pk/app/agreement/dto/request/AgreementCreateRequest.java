package com.pk.app.agreement.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AgreementCreateRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @Size(max = 64) String partnerUserId,
        @NotBlank @Size(max = 128) String deviceNo,
        @NotEmpty @Size(max = 50) @Valid List<AgreementItemRequest> items
) {
}
