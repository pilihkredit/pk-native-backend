package com.pk.infra.credit;

import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.core.credit.port.LenderCreditPort;
import java.time.Instant;

public class CreditStatusPollHandler {
    private static final String SOURCE = "CREDIT_STATUS_POLL";
    private static final String LIMIT_SOURCE = "LENDER_POLL";

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditStatusHistoryRepository creditStatusHistoryRepository;
    private final CreditLimitSnapshotRepository creditLimitSnapshotRepository;
    private final LenderCreditPort lenderCreditPort;
    private final CreditApplyProperties creditApplyProperties;

    public CreditStatusPollHandler(
            CreditApplicationRepository creditApplicationRepository,
            CreditStatusHistoryRepository creditStatusHistoryRepository,
            CreditLimitSnapshotRepository creditLimitSnapshotRepository,
            LenderCreditPort lenderCreditPort,
            CreditApplyProperties creditApplyProperties
    ) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditStatusHistoryRepository = creditStatusHistoryRepository;
        this.creditLimitSnapshotRepository = creditLimitSnapshotRepository;
        this.lenderCreditPort = lenderCreditPort;
        this.creditApplyProperties = creditApplyProperties;
    }

    public void poll(CreditApplicationRepository.CreditApplicationRecord record) {
        LenderCreditPort.LenderCreditStatusResult status = lenderCreditPort.queryStatus(record.applyId());
        String nextStatus = CreditExternalStatusMapper.mapLenderStatus(status.externalStatus());
        if (!nextStatus.equals(record.status())) {
            creditApplicationRepository.updateStatus(
                    record.id(),
                    nextStatus,
                    status.externalStatus(),
                    null
            );
            creditStatusHistoryRepository.insert(
                    record.id(),
                    record.status(),
                    nextStatus,
                    status.externalStatus(),
                    SOURCE
            );
        }
        if (CreditApplicationStatus.APPROVED.equals(nextStatus)) {
            Instant contractExpireAt = status.creditContractExpireTime() == null
                    ? null
                    : Instant.ofEpochMilli(status.creditContractExpireTime());
            creditLimitSnapshotRepository.upsert(new CreditLimitSnapshotRepository.CreditLimitSnapshotData(
                    record.id(),
                    status.riskMinLimit(),
                    status.riskMaxLimit(),
                    status.psychologicalCreditLimit(),
                    status.fakeCreditLimit(),
                    status.borrowAmtStepSize(),
                    contractExpireAt,
                    LIMIT_SOURCE
            ));
            return;
        }
        if (!CreditApplicationStatus.isTerminal(nextStatus)) {
            creditApplicationRepository.scheduleNextPoll(
                    record.id(),
                    Instant.now().plusSeconds(creditApplyProperties.pollIntervalSeconds())
            );
        }
    }
}
