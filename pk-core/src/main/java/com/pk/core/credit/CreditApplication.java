package com.pk.core.credit;

import java.time.Instant;
import java.util.Objects;

public record CreditApplication(
        String applyId,
        String partnerUserId,
        String pkCode,
        long profileVersionId,
        CreditStatus status,
        int version,
        Instant createdAt
) {
    public CreditApplication {
        Objects.requireNonNull(applyId, "applyId is required");
        Objects.requireNonNull(partnerUserId, "partnerUserId is required");
        Objects.requireNonNull(pkCode, "pkCode is required");
        Objects.requireNonNull(status, "status is required");
        Objects.requireNonNull(createdAt, "createdAt is required");
    }
}
