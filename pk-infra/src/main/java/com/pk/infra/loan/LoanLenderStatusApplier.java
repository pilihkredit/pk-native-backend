package com.pk.infra.loan;

import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanLenderStatusQueryRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import java.time.Instant;

public class LoanLenderStatusApplier {
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanStatusHistoryRepository loanStatusHistoryRepository;
    private final LoanLenderStatusQueryRepository loanLenderStatusQueryRepository;
    private final LoanApplyProperties loanApplyProperties;

    public LoanLenderStatusApplier(
            LoanApplicationRepository loanApplicationRepository,
            LoanStatusHistoryRepository loanStatusHistoryRepository,
            LoanLenderStatusQueryRepository loanLenderStatusQueryRepository,
            LoanApplyProperties loanApplyProperties
    ) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanStatusHistoryRepository = loanStatusHistoryRepository;
        this.loanLenderStatusQueryRepository = loanLenderStatusQueryRepository;
        this.loanApplyProperties = loanApplyProperties;
    }

    public void apply(
            LoanApplicationRepository.LoanApplicationRecord record,
            LenderLoanStatusPort.LenderLoanStatusResult status,
            String source
    ) {
        if (LoanApplicationStatus.isTerminal(record.status())) {
            return;
        }
        persistStatusQuerySnapshot(record, status);
        String nextStatus = LoanExternalStatusMapper.mapLenderStatus(status.externalStatus());
        if (!nextStatus.equals(record.status())) {
            loanApplicationRepository.updateStatus(
                    record.id(),
                    nextStatus,
                    status.externalStatus()
            );
            loanStatusHistoryRepository.insert(
                    record.id(),
                    record.status(),
                    nextStatus,
                    status.externalStatus(),
                    source
            );
        }
        if (status.loanApplyNo() != null && !status.loanApplyNo().isBlank()) {
            loanApplicationRepository.markSubmitted(record.id(), status.loanApplyNo(), status.externalStatus());
        }
        if (LoanApplicationStatus.DISBURSED.equals(nextStatus)) {
            loanApplicationRepository.updateDisbursementDetails(
                    record.id(),
                    status.billNo(),
                    status.applyAmt(),
                    status.payAmount(),
                    status.payTime() == null ? null : Instant.ofEpochMilli(status.payTime())
            );
            return;
        }
        if (!LoanApplicationStatus.isTerminal(nextStatus)) {
            loanApplicationRepository.scheduleNextPoll(
                    record.id(),
                    Instant.now().plusSeconds(loanApplyProperties.pollIntervalSeconds())
            );
        }
    }

    private void persistStatusQuerySnapshot(
            LoanApplicationRepository.LoanApplicationRecord record,
            LenderLoanStatusPort.LenderLoanStatusResult status
    ) {
        if (status.requestJson() == null && status.responseDataJson() == null) {
            return;
        }
        loanLenderStatusQueryRepository.upsert(new LoanLenderStatusQueryRepository.LoanLenderStatusQueryData(
                record.loanApplyId(),
                record.profileId(),
                record.mobileNo(),
                record.lenderUserId(),
                firstNonBlank(status.loanApplyNo(), record.externalLoanApplyNo()),
                status.externalStatus(),
                status.billNo(),
                status.applyAmt(),
                status.payAmount(),
                status.payTime() == null ? null : Instant.ofEpochMilli(status.payTime()),
                status.freezeEndTime(),
                status.requestJson(),
                status.responseDataJson(),
                Instant.now()
        ));
    }

    private static String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }
}
