package com.pk.infra.user;

import java.time.Instant;

public record UserProfileRecord(
        long id,
        String partnerUserId,
        String mobileNo,
        String kycStatus,
        Instant createdAt
) {
}
