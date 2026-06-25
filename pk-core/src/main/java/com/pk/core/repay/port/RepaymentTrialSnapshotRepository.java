package com.pk.core.repay.port;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RepaymentTrialSnapshotRepository {
    TrialSnapshotRecord insert(TrialSnapshotInsert insert, List<TrialOrderInsert> orders);

    Optional<TrialSnapshotRecord> findByTrialNo(String trialNo);

    record TrialSnapshotInsert(
            String trialNo,
            long profileId,
            String trialType,
            Integer totalBillCount,
            BigDecimal totalShouldAmount,
            BigDecimal totalReductionAmount,
            String defaultVaJson,
            String rawResponseJson
    ) {
    }

    record TrialOrderInsert(
            long loanApplicationId,
            String loanApplyId,
            boolean settle,
            String termNosJson,
            BigDecimal shouldAmount,
            String billStatus,
            String termInfoJson
    ) {
    }

    record TrialSnapshotRecord(
            long id,
            String trialNo,
            long profileId,
            String trialType,
            Integer totalBillCount,
            BigDecimal totalShouldAmount,
            BigDecimal totalReductionAmount,
            String defaultVaJson,
            String rawResponseJson,
            Instant createdAt
    ) {
    }
}
