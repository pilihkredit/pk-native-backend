package com.pk.infra.credit;

import com.pk.core.credit.CreditApplicationStatus;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.CreditLimitSnapshotRepository;
import com.pk.core.credit.port.CreditStatusHistoryRepository;
import com.pk.core.credit.port.LenderCreditPort;
import java.time.Instant;

public class CreditApplyHandler {
    private static final String SOURCE = "CREDIT_APPLY_WORKER";
    private static final String EXTERNAL_PROCESSING = "PROCESSING";

    private final CreditApplicationRepository creditApplicationRepository;
    private final CreditStatusHistoryRepository creditStatusHistoryRepository;
    private final LenderCreditPort lenderCreditPort;
    private final CreditApplyProperties creditApplyProperties;

    public CreditApplyHandler(
            CreditApplicationRepository creditApplicationRepository,
            CreditStatusHistoryRepository creditStatusHistoryRepository,
            LenderCreditPort lenderCreditPort,
            CreditApplyProperties creditApplyProperties
    ) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.creditStatusHistoryRepository = creditStatusHistoryRepository;
        this.lenderCreditPort = lenderCreditPort;
        this.creditApplyProperties = creditApplyProperties;
    }

    public String submit(CreditApplyJob job) {
        String mobileNo = creditApplicationRepository.findById(job.creditApplicationId())
                .orElseThrow(() -> new IllegalStateException("Credit application not found: " + job.creditApplicationId()))
                .mobileNo();
        transition(job.creditApplicationId(), mobileNo, CreditApplicationStatus.INIT, CreditApplicationStatus.SUBMITTING, null);

        LenderCreditPort.LenderCreditApplyResult result = lenderCreditPort.apply(
                new LenderCreditPort.LenderCreditApplyCommand(
                        job.applyId(),
                        job.partnerUserId(),
                        job.lat(),
                        job.lng(),
                        job.ip(),
                        job.address(),
                        job.device(),
                        job.appList()
                )
        );

        creditApplicationRepository.markSubmitted(
                job.creditApplicationId(),
                result.creditApplyNo(),
                EXTERNAL_PROCESSING
        );
        creditStatusHistoryRepository.insert(
                job.creditApplicationId(),
                mobileNo,
                CreditApplicationStatus.SUBMITTING,
                CreditApplicationStatus.PROCESSING,
                EXTERNAL_PROCESSING,
                SOURCE
        );
        creditApplicationRepository.scheduleNextPoll(
                job.creditApplicationId(),
                Instant.now().plusSeconds(creditApplyProperties.pollIntervalSeconds())
        );
        return result.creditApplyNo();
    }

    private void transition(
            long creditApplicationId,
            String mobileNo,
            String fromStatus,
            String toStatus,
            String externalStatus
    ) {
        creditApplicationRepository.updateStatus(creditApplicationId, toStatus, externalStatus, null);
        creditStatusHistoryRepository.insert(creditApplicationId, mobileNo, fromStatus, toStatus, externalStatus, SOURCE);
    }
}
