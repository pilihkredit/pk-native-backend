package com.pk.core.credit.port;

import java.util.Optional;

public interface CreditApplicationRepository {
    Optional<CreditApplicationRecord> findById(long id);

    Optional<CreditApplicationRecord> findByRequestId(String requestId);

    Optional<CreditApplicationRecord> findByApplyIdAndUserId(String applyId, long userId);

    Optional<CreditApplicationRecord> findLatestByUserId(long userId);

    Optional<CreditApplicationRecord> findByApplyId(String applyId);

    long insert(CreditApplicationInsert insert);

    void updateApplyNo(long id, String applyNo);

    void updateLastLenderInteraction(long id, Long externalInteractionId);

    record CreditApplicationInsert(
            String applyId,
            String requestId,
            String providerCode,
            long userId
    ) {
    }

    record CreditApplicationRecord(
            long id,
            String applyId,
            String requestId,
            String providerCode,
            long userId,
            String partnerUserId,
            String applyNo
    ) {
    }
}
