package com.pk.app.agreement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record AgreementLatestRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @Size(max = 50) List<@Size(max = 64) String> agreementTypes
) {
}
