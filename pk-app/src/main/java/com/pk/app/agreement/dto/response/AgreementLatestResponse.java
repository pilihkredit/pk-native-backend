package com.pk.app.agreement.dto.response;

import java.util.List;

public record AgreementLatestResponse(
        List<AgreementRecordResponse> records
) {
}
