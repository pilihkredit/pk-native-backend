package com.pk.core.repay.port;

import com.pk.core.repay.LenderRepayTrialResult;
import com.pk.core.repay.LenderRepayVa;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RepaymentTrialSnapshotRepository {
    /**
     * Replace trial data for the given loan_apply_id(s): delete previous order trees
     * (and orphan snapshots), then insert a new snapshot + orders.
     * Each loan_apply_id keeps at most one latest trial order.
     */
    TrialSnapshotRecord replace(TrialSnapshotInsert insert, List<TrialOrderInsert> orders);

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
            Long externalInteractionId,
            String source
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
