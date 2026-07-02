package com.pk.infra.credit;

import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.core.credit.port.LenderCreditPort;
import java.time.Instant;

public class CreditLenderStatusApplier {
    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditStatusHistoryRepository creditStatusHistoryRepository;
    private final CreditLimitSnapshotRepository creditLimitSnapshotRepository;
    private final CreditApplyProperties creditApplyProperties;

    public CreditLenderStatusApplier(
            CreditApplicationRepository creditApplicationRepository,
            CreditStatusHistoryRepository creditStatusHistoryRepository,
            CreditLimitSnapshotRepository creditLimitSnapshotRepository,
            CreditApplyProperties creditApplyProperties
    ) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditStatusHistoryRepository = creditStatusHistoryRepository;
        this.creditLimitSnapshotRepository = creditLimitSnapshotRepository;
        this.creditApplyProperties = creditApplyProperties;
    }

    public void apply(
            CreditApplicationRepository.CreditApplicationRecord record,
            LenderCreditPort.LenderCreditStatusResult status,
            String source,
            String limitSource
    ) {
        String nextStatus = CreditExternalStatusMapper.mapLenderStatus(status.externalStatus());
        if (CreditApplicationStatus.isTerminal(record.status())) {
            return;
        }
        if (!nextStatus.equals(record.status())) {
            creditApplicationRepository.updateStatus(
                    record.id(),
                    nextStatus,
                    status.externalStatus(),
                    null
            );
            creditStatusHistoryRepository.insert(
                    record.id(),
                    record.mobileNo(),
                    record.status(),
                    nextStatus,
                    status.externalStatus(),
                    source
            );
        }
        if (status.creditApplyNo() != null && !status.creditApplyNo().isBlank()) {
            creditApplicationRepository.markSubmitted(record.id(), status.creditApplyNo(), status.externalStatus());
        }
        if (CreditApplicationStatus.APPROVED.equals(nextStatus)) {
            Instant contractExpireAt = status.creditContractExpireTime() == null
                    ? null
                    : Instant.ofEpochMilli(status.creditContractExpireTime());
            creditLimitSnapshotRepository.upsert(new CreditLimitSnapshotRepository.CreditLimitSnapshotData(
                    record.id(),
                    record.mobileNo(),
                    status.riskMinLimit(),
                    status.riskMaxLimit(),
                    status.psychologicalCreditLimit(),
                    status.fakeCreditLimit(),
                    status.borrowAmtStepSize(),
                    contractExpireAt,
                    limitSource
            ));
            return;
        }
        if (CreditApplicationStatus.REJECTED.equals(nextStatus) && status.freezeEndTime() != null) {
            creditApplicationRepository.updateFreezeEndAt(
                    record.id(),
                    Instant.ofEpochMilli(status.freezeEndTime())
            );
        }
        if (!CreditApplicationStatus.isTerminal(nextStatus)) {
            creditApplicationRepository.scheduleNextPoll(
                    record.id(),
                    Instant.now().plusSeconds(creditApplyProperties.pollIntervalSeconds())
            );
        }
    }
}
