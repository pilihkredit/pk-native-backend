package com.pk.core.loan.port;

import com.pk.core.loan.LenderTrialTerm;
import java.math.BigDecimal;
import java.util.List;

public interface LenderLoanTrialPort {
    LenderLoanTrialResult trial(LenderLoanTrialCommand command);

    record LenderLoanTrialCommand(
            String applyId,
            BigDecimal applyAmt,
            String productCode,
            String repayMethod,
            Long couponId
    ) {
    }

    record LenderLoanTrialResult(
            BigDecimal applyAmt,
            BigDecimal payAmount,
            BigDecimal schdAmount,
            BigDecimal interest,
            Integer loanTerm,
            BigDecimal loanPrincipal,
            Integer totalDays,
            List<LenderTrialTerm> termInfo,
            String rawResponseJson
    ) {
    }
}
