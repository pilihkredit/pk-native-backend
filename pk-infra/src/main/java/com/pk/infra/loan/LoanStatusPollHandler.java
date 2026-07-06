package com.pk.infra.loan;

import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.external.LenderInteractionContext;

public class LoanStatusPollHandler {
    private static final String POLL_SOURCE = "LOAN_STATUS_POLL";
    private static final String API_SOURCE = "LOAN_STATUS_API";

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
        syncFromLender(record, POLL_SOURCE);
    }

    public void syncFromLenderForApi(LoanApplicationRepository.LoanApplicationRecord record) {
        syncFromLender(record, API_SOURCE);
    }

    private void syncFromLender(
            LoanApplicationRepository.LoanApplicationRecord record,
            String source
    ) {
        LenderLoanStatusPort.LenderLoanStatusResult status = LenderInteractionContext.runWithMobileNo(
                record.mobileNo(),
                () -> lenderLoanStatusPort.queryStatus(record.loanApplyId())
        );
        loanLenderStatusApplier.apply(record, status, source);
    }
}
