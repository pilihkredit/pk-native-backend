package com.pk.app.agreement.dto.response;

import java.util.List;

public record AgreementCreateResponse(
        List<AgreementRecordResponse> records
) {
}
