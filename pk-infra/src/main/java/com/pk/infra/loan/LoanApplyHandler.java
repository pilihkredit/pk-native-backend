package com.pk.infra.loan;

import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.port.LenderLoanApplyPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import com.pk.core.external.LenderInteractionContext;
import java.time.Instant;

public class LoanApplyHandler {
    private static final String SOURCE = "LOAN_APPLY_WORKER";
    private static final String EXTERNAL_PROCESSING = "PROCESSING";

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanStatusHistoryRepository loanStatusHistoryRepository;
    private final LenderLoanApplyPort lenderLoanApplyPort;
    private final LoanApplyProperties loanApplyProperties;

    public LoanApplyHandler(
            LoanApplicationRepository loanApplicationRepository,
            LoanStatusHistoryRepository loanStatusHistoryRepository,
            LenderLoanApplyPort lenderLoanApplyPort,
            LoanApplyProperties loanApplyProperties
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanStatusHistoryRepository = loanStatusHistoryRepository;
        this.lenderLoanApplyPort = lenderLoanApplyPort;
        this.loanApplyProperties = loanApplyProperties;
    }

    public String submit(LoanApplyJob job) {
        if (loanApplicationRepository.findById(job.loanApplicationId()).isEmpty()) {
            throw new IllegalStateException("Loan application not found: " + job.loanApplicationId());
        }
        transition(job.loanApplicationId(), LoanApplicationStatus.INIT, LoanApplicationStatus.SUBMITTING, null);

        LenderLoanApplyPort.LenderLoanApplyResult result = LenderInteractionContext.runWithMobileNo(
                job.mobileNo(),
                () -> lenderLoanApplyPort.apply(
                        new LenderLoanApplyPort.LenderLoanApplyCommand(
                                job.creditApplyId(),
                                job.loanApplyId(),
                                job.applyAmt(),
                                job.productCode(),
                                job.repayMethod(),
                                job.loanPurpose(),
                                job.couponId(),
                                job.lat(),
                                job.lng(),
                                job.ip(),
                                job.address(),
                                job.adId(),
                                job.device(),
                                job.appList()
                        )
                )
        );

        loanApplicationRepository.markLenderApplySubmitted(new LoanApplicationRepository.LenderApplySubmitted(
                job.loanApplicationId(),
                result.loanApplyNo(),
                result.lenderUserId(),
                result.externalStatus(),
                result.externalInteractionId()
        ));
        loanStatusHistoryRepository.insert(
                job.loanApplicationId(),
                LoanApplicationStatus.SUBMITTING,
                LoanApplicationStatus.PROCESSING,
                EXTERNAL_PROCESSING,
                SOURCE
        );
        loanApplicationRepository.scheduleNextPoll(
                job.loanApplicationId(),
                Instant.now().plusSeconds(loanApplyProperties.pollIntervalSeconds())
        );
        return result.loanApplyNo();
    }

    private void transition(long id, String from, String to, String externalStatus) {
        loanApplicationRepository.updateStatus(id, to, externalStatus);
        loanStatusHistoryRepository.insert(id, from, to, externalStatus, SOURCE);
    }
}
