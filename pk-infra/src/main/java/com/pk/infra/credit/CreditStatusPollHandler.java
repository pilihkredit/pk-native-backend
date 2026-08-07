package com.pk.infra.credit;

import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.external.DataWriteSource;
import com.pk.core.external.LenderInteractionContext;

public class CreditStatusPollHandler {
    private final LenderCreditPort lenderCreditPort;
    private final CreditLenderStatusApplier creditLenderStatusApplier;
    private final UserAuthRepository userAuthRepository;

    public CreditStatusPollHandler(
            LenderCreditPort lenderCreditPort,
            CreditLenderStatusApplier creditLenderStatusApplier,
            UserAuthRepository userAuthRepository
    ) {
        this.lenderCreditPort = lenderCreditPort;
        this.creditLenderStatusApplier = creditLenderStatusApplier;
        this.userAuthRepository = userAuthRepository;
    }

    public void syncFromLenderForApi(CreditApplicationRepository.CreditApplicationRecord record) {
        syncFromLender(record, DataWriteSource.APP);
    }

    public void syncFromLenderForJob(CreditApplicationRepository.CreditApplicationRecord record) {
        syncFromLender(record, DataWriteSource.JOB);
    }

    private void syncFromLender(CreditApplicationRepository.CreditApplicationRecord record, String source) {
        String mobileNo = userAuthRepository.findByUserId(record.userId())
                .map(profile -> profile.mobileNo())
                .orElse(null);
        LenderCreditPort.LenderCreditStatusResult status = LenderInteractionContext.runWith(
                mobileNo,
                source,
                () -> lenderCreditPort.queryStatus(record.applyId())
        );
        creditLenderStatusApplier.apply(record, status, source);
    }
}
