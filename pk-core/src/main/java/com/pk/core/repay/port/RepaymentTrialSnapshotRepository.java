package com.pk.core.repay.port;

import com.pk.core.repay.LenderRepayTrialResult;
import com.pk.core.repay.LenderRepayVa;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RepaymentTrialSnapshotRepository {
    TrialSnapshotRecord insert(TrialSnapshotInsert insert, List<TrialOrderInsert> orders);

    Optional<TrialSnapshotRecord> findByTrialNo(String trialNo);

    record TrialSnapshotInsert(
            String trialNo,
            long userId,
            String trialType,
            Integer totalBillCount,
            BigDecimal totalShouldAmount,
            BigDecimal totalReductionAmount,
            BigDecimal totalPaidAmount,
            LenderRepayVa defaultVa,
            LenderRepayVa spareVa,
            LenderRepayVa disabledDefaultVa,
            Long externalInteractionId
    ) {
    }

    record TrialOrderInsert(
            long loanApplicationId,
            boolean settle,
            String termNosJson,
            LenderRepayTrialResult bill
    ) {
    }

    record TrialSnapshotRecord(
            long id,
            String trialNo,
            long userId,
            String trialType,
            Integer totalBillCount,
            BigDecimal totalShouldAmount,
            BigDecimal totalReductionAmount,
            BigDecimal totalPaidAmount,
            Long externalInteractionId,
            Instant createdAt
    ) {
    }
}
