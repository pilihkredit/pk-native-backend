package com.pk.infra.loan;

import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;

public class LoanStatusPollHandler {
    private static final String SOURCE = "LOAN_STATUS_POLL";

    private final LenderLoanStatusPort lenderLoanStatusPort;
    private final LoanLenderStatusApplier loanLenderStatusApplier;

    public LoanStatusPollHandler(
            LenderLoanStatusPort lenderLoanStatusPort,
            LoanLenderStatusApplier loanLenderStatusApplier
    ) {
        this.lenderLoanStatusPort = lenderLoanStatusPort;
        this.loanLenderStatusApplier = loanLenderStatusApplier;
    }

    public void poll(LoanApplicationRepository.LoanApplicationRecord record) {
        LenderLoanStatusPort.LenderLoanStatusResult status = lenderLoanStatusPort.queryStatus(record.loanApplyId());
        loanLenderStatusApplier.apply(record, status, SOURCE);
    }
}
