package com.pk.infra.loan.repository;

import com.pk.core.loan.port.LoanApplicationRepository.LenderApplySubmitted;

public class LenderApplySubmittedParam {
    private long id;
    private String externalLoanApplyNo;
    private String lenderUserId;
    private String externalStatus;
    private Long externalInteractionId;

    public static LenderApplySubmittedParam from(LenderApplySubmitted submitted) {
        LenderApplySubmittedParam p = new LenderApplySubmittedParam();
        p.id = submitted.id();
        p.externalLoanApplyNo = submitted.externalLoanApplyNo();
        p.lenderUserId = submitted.lenderUserId();
        p.externalStatus = submitted.externalStatus();
        p.externalInteractionId = submitted.externalInteractionId();
        return p;
    }

    public long getId() {
        return id;
    }

    public String getExternalLoanApplyNo() {
        return externalLoanApplyNo;
    }

    public String getLenderUserId() {
        return lenderUserId;
    }

    public String getExternalStatus() {
        return externalStatus;
    }

    public Long getExternalInteractionId() {
        return externalInteractionId;
    }
}
