package com.pk.app.agreement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgreementItemRequest(
        @NotBlank @Size(max = 64) String agreementType,
        Boolean agreed
) {
}
