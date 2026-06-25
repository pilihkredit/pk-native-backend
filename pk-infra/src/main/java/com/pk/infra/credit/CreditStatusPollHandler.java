package com.pk.infra.credit;

import com.pk.core.credit.port.CreditApplicationRepository;
import com.pk.core.credit.port.LenderCreditPort;

public class CreditStatusPollHandler {
    private static final String SOURCE = "CREDIT_STATUS_POLL";
    private static final String LIMIT_SOURCE = "LENDER_POLL";

    private final LenderCreditPort lenderCreditPort;
    private final CreditLenderStatusApplier creditLenderStatusApplier;

    public CreditStatusPollHandler(
            LenderCreditPort lenderCreditPort,
            CreditLenderStatusApplier creditLenderStatusApplier
    ) {
        this.lenderCreditPort = lenderCreditPort;
        this.creditLenderStatusApplier = creditLenderStatusApplier;
    }

    public void poll(CreditApplicationRepository.CreditApplicationRecord record) {
        LenderCreditPort.LenderCreditStatusResult status = lenderCreditPort.queryStatus(record.applyId());
        creditLenderStatusApplier.apply(record, status, SOURCE, LIMIT_SOURCE);
    }
}
