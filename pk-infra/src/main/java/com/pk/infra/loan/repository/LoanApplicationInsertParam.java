package com.pk.infra.loan.repository;
import com.pk.core.loan.port.LoanApplicationRepository.LoanApplicationInsert;
import java.math.BigDecimal;
public class LoanApplicationInsertParam {
    private String loanApplyId; private long creditApplicationId; private long quoteId;
    private long profileId; private long profileVersionId; private String status;
    private BigDecimal applyAmt; private String loanPurpose;
    public static LoanApplicationInsertParam from(LoanApplicationInsert insert) {
        LoanApplicationInsertParam p = new LoanApplicationInsertParam();
        p.loanApplyId = insert.loanApplyId(); p.creditApplicationId = insert.creditApplicationId();
        p.quoteId = insert.quoteId(); p.profileId = insert.profileId();
        p.profileVersionId = insert.profileVersionId(); p.status = insert.status();
        p.applyAmt = insert.applyAmt(); p.loanPurpose = insert.loanPurpose(); return p;
    }
    public String getLoanApplyId() { return loanApplyId; } public long getCreditApplicationId() { return creditApplicationId; }
    public long getQuoteId() { return quoteId; } public long getProfileId() { return profileId; }
    public long getProfileVersionId() { return profileVersionId; } public String getStatus() { return status; }
    public BigDecimal getApplyAmt() { return applyAmt; } public String getLoanPurpose() { return loanPurpose; }
}
