package com.pk.infra.credit;

import com.pk.core.auth.port.UserAuthRepository;
import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.LenderCreditPort;
import com.pk.core.external.LenderInteractionContext;

public class CreditStatusPollHandler {
    private static final String API_SOURCE = "CREDIT_STATUS_API";
    private static final String API_LIMIT_SOURCE = "LENDER_API";

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
        String mobileNo = userAuthRepository.findByUserId(record.userId())
                .map(profile -> profile.mobileNo())
                .orElse(null);
        LenderCreditPort.LenderCreditStatusResult status = LenderInteractionContext.runWithMobileNo(
                mobileNo,
                () -> lenderCreditPort.queryStatus(record.applyId())
        );
        creditLenderStatusApplier.apply(record, status, API_SOURCE, API_LIMIT_SOURCE);
    }
}
