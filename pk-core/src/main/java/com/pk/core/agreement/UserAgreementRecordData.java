package com.pk.core.agreement;

import java.time.Instant;

public record UserAgreementRecordData(
        long id,
        String mobileNo,
        String partnerUserId,
        String deviceNo,
        Long profileId,
        String agreementType,
        Boolean agreed,
        Instant agreedAt,
        Long clickedAtMs
) {
}
