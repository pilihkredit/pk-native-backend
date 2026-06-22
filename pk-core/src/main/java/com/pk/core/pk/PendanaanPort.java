package com.pk.core.pk;

import com.pk.core.credit.CreditApplication;
import com.pk.core.loan.LoanApplication;

public interface PendanaanPort {
    String pkCode();

    PkSubmitResult submitCredit(CreditApplication application);

    PkSubmitResult submitLoan(LoanApplication application);
}
