package com.pk.core.agreement;

import java.time.Instant;

public record UserAgreementRecordData(
        long id,
        String partnerUserId,
        String deviceNo,
        Long userId,
        String agreementType,
        Boolean agreed,
        Instant agreedAt,
        Long clickedAtMs,
        String mobileNo
) {
}
