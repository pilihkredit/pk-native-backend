package com.pk.infra.loan;

import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.external.DataWriteSource;
import com.pk.core.external.LenderInteractionContext;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;

public class LoanStatusPollHandler {
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
        syncFromLender(record, DataWriteSource.JOB);
    }

    public void syncFromLenderForApi(LoanApplicationRepository.LoanApplicationRecord record) {
        syncFromLender(record, DataWriteSource.APP);
    }

    public void syncFromLenderForJob(LoanApplicationRepository.LoanApplicationRecord record) {
        syncFromLender(record, DataWriteSource.JOB);
    }

    private void syncFromLender(
            LoanApplicationRepository.LoanApplicationRecord record,
            String source
    ) {
        String mobileNo = userAuthRepository.findByUserId(record.userId())
                .map(profile -> profile.mobileNo())
                .orElse(null);
        LenderLoanStatusPort.LenderLoanStatusResult status = LenderInteractionContext.runWith(
                mobileNo,
                source,
                () -> lenderLoanStatusPort.queryStatus(record.loanApplyId())
        );
        loanLenderStatusApplier.apply(record, status, source);
    }
}
