package com.pk.infra.loan;

import com.pk.core.loan.LoanApplicationStatus;
import com.pk.core.loan.port.LenderLoanStatusPort;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.core.loan.port.LoanLenderStatusQueryRepository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

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
        apply(record, status, source, null);
    }

    public void apply(
            LoanApplicationRepository.LoanApplicationRecord record,
            LenderLoanStatusPort.LenderLoanStatusResult status,
            String source,
            Long externalInteractionCallbackId
    ) {
        if (LoanApplicationStatus.isTerminal(record.status())) {
            return;
        }
        persistStatusQuerySnapshot(record, status, externalInteractionCallbackId);
        applyMainRecordInternal(record, status, source);
    }

    /**
     * Updates {@code loan_application} from a lender status payload without touching
     * {@code loan_lender_status_query}. Used when the status snapshot was already persisted.
     */
    public void applyMainRecord(
            LoanApplicationRepository.LoanApplicationRecord record,
            LenderLoanStatusPort.LenderLoanStatusResult status,
            String source
    ) {
        if (LoanApplicationStatus.isTerminal(record.status())) {
            return;
        }
        applyMainRecordInternal(record, status, source);
    }

    private void applyMainRecordInternal(
            LoanApplicationRepository.LoanApplicationRecord record,
            LenderLoanStatusPort.LenderLoanStatusResult status,
            String source
    ) {
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
            LenderLoanStatusPort.LenderLoanStatusResult status,
            Long externalInteractionCallbackId
    ) {
        Optional<LoanLenderStatusQueryRepository.LoanLenderStatusQueryData> latest =
                loanLenderStatusQueryRepository.findLatestByLoanApplyId(record.loanApplyId());

        LoanLenderStatusQueryRepository.LoanLenderStatusQueryData next =
                new LoanLenderStatusQueryRepository.LoanLenderStatusQueryData(
                        record.loanApplyId(),
                        record.userId(),
                        record.mobileNo(),
                        record.lenderUserId(),
                        firstNonBlank(status.loanApplyNo(), record.externalLoanApplyNo()),
                        status.externalStatus(),
                        status.billNo(),
                        status.applyAmt(),
                        status.payAmount(),
                        status.payTime() == null ? null : Instant.ofEpochMilli(status.payTime()),
                        status.freezeEndTime(),
                        status.externalInteractionId(),
                        externalInteractionCallbackId,
                        Instant.now()
                );

        if (latest.isPresent() && !hasBusinessChange(latest.get(), next)) {
            return;
        }
        loanLenderStatusQueryRepository.insert(next);
    }

    static boolean hasBusinessChange(
            LoanLenderStatusQueryRepository.LoanLenderStatusQueryData previous,
            LoanLenderStatusQueryRepository.LoanLenderStatusQueryData next
    ) {
        return !Objects.equals(normalizeText(previous.externalStatus()), normalizeText(next.externalStatus()))
                || !Objects.equals(normalizeText(previous.externalLoanApplyNo()), normalizeText(next.externalLoanApplyNo()))
                || !Objects.equals(normalizeText(previous.billNo()), normalizeText(next.billNo()))
                || !Objects.equals(normalizeText(previous.lenderUserId()), normalizeText(next.lenderUserId()))
                || !decimalEquals(previous.applyAmt(), next.applyAmt())
                || !decimalEquals(previous.payAmount(), next.payAmount())
                || !Objects.equals(previous.payTime(), next.payTime())
                || !Objects.equals(previous.freezeEndTime(), next.freezeEndTime());
    }

    private static String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean decimalEquals(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return left.compareTo(right) == 0;
    }

    private static String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.isBlank() ? fallback : primary;
    }
}
