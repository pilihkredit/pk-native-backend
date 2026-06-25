package com.pk.core.repay.port;

import com.pk.core.repay.LenderRepayTrialResult;
import java.util.List;

public interface LenderRepayTrialPort {
    LenderRepayTrialResult trial(LenderRepayTrialCommand command);

    LenderRepayTrialBatchResult trialBatch(LenderRepayTrialBatchCommand command);

    record LenderRepayTrialCommand(
            String loanApplyId,
            boolean settle,
            List<Integer> termNos
    ) {
    }

    record LenderRepayTrialBatchCommand(
            List<LenderRepayTrialCommand> repayOrders
    ) {
    }

    record LenderRepayTrialBatchResult(
            Integer totalBillCount,
            java.math.BigDecimal totalShouldAmount,
            java.math.BigDecimal totalReductionAmount,
            java.math.BigDecimal totalPaidAmount,
            com.pk.core.repay.LenderRepayVa defaultVa,
            com.pk.core.repay.LenderRepayVa spareVa,
            com.pk.core.repay.LenderRepayVa disabledDefaultVa,
            List<LenderRepayTrialResult> billTrials,
            String rawResponseJson
    ) {
    }
}
