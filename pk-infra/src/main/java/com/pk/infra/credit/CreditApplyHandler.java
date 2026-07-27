package com.pk.infra.credit;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.external.LenderInteractionContext;

public class CreditApplyHandler {
    private final CreditApplicationRepository creditApplicationRepository;
    private final LenderCreditPort lenderCreditPort;

    public CreditApplyHandler(
            CreditApplicationRepository creditApplicationRepository,
            LenderCreditPort lenderCreditPort
    ) {
        this.creditApplicationRepository = creditApplicationRepository;
        this.lenderCreditPort = lenderCreditPort;
    }

    public String submit(CreditApplyJob job) {
        if (creditApplicationRepository.findById(job.creditApplicationId()).isEmpty()) {
            throw new IllegalStateException("Credit application not found: " + job.creditApplicationId());
        }

        LenderCreditPort.LenderCreditApplyResult result = LenderInteractionContext.runWithMobileNo(
                job.mobileNo(),
                () -> lenderCreditPort.apply(
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
                )
        );

        if (result.externalInteractionId() != null) {
            creditApplicationRepository.updateLastLenderInteraction(
                    job.creditApplicationId(),
                    result.externalInteractionId()
            );
        }
        if (result.creditApplyNo() != null && !result.creditApplyNo().isBlank()) {
            creditApplicationRepository.updateApplyNo(job.creditApplicationId(), result.creditApplyNo());
        }
        return result.creditApplyNo();
    }
}
