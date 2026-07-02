package com.pk.core.credit.port;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CreditApplicationRepository {
    Optional<CreditApplicationRecord> findById(long id);

    Optional<CreditApplicationRecord> findByRequestId(String requestId);

    Optional<CreditApplicationRecord> findByApplyIdAndProfileId(String applyId, long profileId);

    Optional<CreditApplicationRecord> findByApplyId(String applyId);

    long insert(CreditApplicationInsert insert);

    void updateStatus(long id, String status, String externalStatus, String lastErrorCode);

    void markSubmitted(long id, String externalCreditApplyNo, String externalStatus);

    void scheduleNextPoll(long id, Instant nextPollAt);

    void updateFreezeEndAt(long id, Instant freezeEndAt);

    void updateLastLenderAudit(long id, String requestJson, String responseJson);

    List<CreditApplicationRecord> findDueForPoll(int limit);

    record CreditApplicationInsert(
            String applyId,
            String requestId,
            String providerCode,
            long profileId,
            String mobileNo,
            long profileVersionId,
            String status
    ) {
    }

    record CreditApplicationRecord(
            long id,
            String applyId,
            String requestId,
            String providerCode,
            long profileId,
            String mobileNo,
            long profileVersionId,
            String externalCreditApplyNo,
            String status,
            String externalStatus,
            Instant freezeEndAt
    ) {
    }
}
