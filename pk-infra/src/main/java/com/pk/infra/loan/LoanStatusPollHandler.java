package com.pk.infra.loan;

import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.external.LenderInteractionContext;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;

public class LoanStatusPollHandler {
    private static final String POLL_SOURCE = "LOAN_STATUS_POLL";
    private static final String API_SOURCE = "LOAN_STATUS_API";

    private final LenderLoanStatusPort lenderLoanStatusPort;
    private final LoanLenderStatusApplier loanLenderStatusApplier;
    private final UserAuthRepository userAuthRepository;

    public LoanStatusPollHandler(
            LenderLoanStatusPort lenderLoanStatusPort,
            LoanLenderStatusApplier loanLenderStatusApplier,
            UserAuthRepository userAuthRepository
    ) {
        this.lenderLoanStatusPort = lenderLoanStatusPort;
        this.loanLenderStatusApplier = loanLenderStatusApplier;
        this.userAuthRepository = userAuthRepository;
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
        String mobileNo = userAuthRepository.findByUserId(record.userId())
                .map(profile -> profile.mobileNo())
                .orElse(null);
        LenderLoanStatusPort.LenderLoanStatusResult status = LenderInteractionContext.runWithMobileNo(
                mobileNo,
                () -> lenderLoanStatusPort.queryStatus(record.loanApplyId())
        );
        loanLenderStatusApplier.apply(record, status, source);
    }
}
