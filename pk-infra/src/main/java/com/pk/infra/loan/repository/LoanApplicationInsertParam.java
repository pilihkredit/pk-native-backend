package com.pk.infra.loan.repository;

import com.pk.core.loan.port.LoanApplicationRepository.LoanApplicationInsert;
import java.math.BigDecimal;

public class LoanApplicationInsertParam {
    private String loanApplyId;
    private String requestId;
    private String applyId;
    private String mobileNo;
    private long creditApplicationId;
    private Long quoteId;
    private String quoteNo;
    private long profileId;
    private long profileVersionId;
    private String status;
    private BigDecimal applyAmt;
    private String loanPurpose;

    public static LoanApplicationInsertParam from(LoanApplicationInsert insert) {
        LoanApplicationInsertParam p = new LoanApplicationInsertParam();
        p.loanApplyId = insert.loanApplyId();
        p.requestId = insert.requestId();
        p.applyId = insert.applyId();
        p.mobileNo = insert.mobileNo();
        p.creditApplicationId = insert.creditApplicationId();
        p.quoteId = insert.quoteId();
        p.quoteNo = insert.quoteNo();
        p.profileId = insert.profileId();
        p.profileVersionId = insert.profileVersionId();
        p.status = insert.status();
        p.applyAmt = insert.applyAmt();
        p.loanPurpose = insert.loanPurpose();
        return p;
    }

    public String getLoanApplyId() {
        return loanApplyId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getApplyId() {
        return applyId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public long getCreditApplicationId() {
        return creditApplicationId;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public String getQuoteNo() {
        return quoteNo;
    }

    public long getProfileId() {
        return profileId;
    }

    public long getProfileVersionId() {
        return profileVersionId;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getApplyAmt() {
        return applyAmt;
    }

    public String getLoanPurpose() {
        return loanPurpose;
    }
}
