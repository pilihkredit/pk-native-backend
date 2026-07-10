package com.pk.core.loan.port;

import com.pk.core.loan.LenderTrialTerm;
import com.pk.core.loan.LoanTrialQuoteDetail;
import java.util.List;

public interface LenderLoanTrialPort {
    LenderLoanTrialResult trial(LenderLoanTrialCommand command);

    record LenderLoanTrialCommand(
            String applyId,
            java.math.BigDecimal applyAmt,
            String productCode,
            String repayMethod,
            Long couponId
    ) {
    }

    record LenderLoanTrialResult(
            LoanTrialQuoteDetail quote,
            List<LenderTrialTerm> termInfo,
            String requestJson,
            String rawResponseJson
    ) {
    }
}
