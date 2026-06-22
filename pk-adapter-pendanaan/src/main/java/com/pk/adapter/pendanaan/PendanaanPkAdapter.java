package com.pk.adapter.pendanaan;

import com.pk.core.credit.CreditApplication;
import com.pk.core.loan.LoanApplication;
import com.pk.core.pk.PendanaanPort;
import com.pk.core.pk.PkSubmitResult;
import org.springframework.stereotype.Component;

@Component
public class PendanaanPkAdapter implements PendanaanPort {
    public static final String CODE = "PENDANAAN";

    @Override
    public String pkCode() {
        return CODE;
    }

    @Override
    public PkSubmitResult submitCredit(CreditApplication application) {
        return PkSubmitResult.accepted(application.applyId());
    }

    @Override
    public PkSubmitResult submitLoan(LoanApplication application) {
        return PkSubmitResult.accepted(application.loanApplyId());
    }
}
