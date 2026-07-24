package com.pk.core.credit.port;

import java.util.Optional;

public interface CreditApplicationRepository {
    Optional<CreditApplicationRecord> findById(long id);

    Optional<CreditApplicationRecord> findByRequestId(String requestId);

    Optional<CreditApplicationRecord> findByApplyIdAndProfileId(String applyId, long profileId);

    Optional<CreditApplicationRecord> findLatestByProfileId(long profileId);

    Optional<CreditApplicationRecord> findByApplyId(String applyId);

    long insert(CreditApplicationInsert insert);

    void updateApplyNo(long id, String applyNo);

    void updateLastLenderInteraction(long id, Long externalInteractionId);

    record CreditApplicationInsert(
            String applyId,
            String requestId,
            String providerCode,
            long profileId,
            String mobileNo
    ) {
    }

    record CreditApplicationRecord(
            long id,
            String applyId,
            String requestId,
            String providerCode,
            long profileId,
            String partnerUserId,
            String mobileNo,
            String applyNo
    ) {
    }
}
