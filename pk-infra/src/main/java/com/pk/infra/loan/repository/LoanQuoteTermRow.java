package com.pk.infra.loan.repository;

import java.math.BigDecimal;
public class LoanQuoteTermRow {
    private long quoteId;
    private long userId;
    private int termNo;
    private Long valueDate;
    private Long dueDate;
    private Long graceDate;
    private BigDecimal schdAmount;
    private BigDecimal schdPrincipal;
    private BigDecimal showLoanPrincipal;
    private BigDecimal schdInterest;
    private BigDecimal showInterest;
    private BigDecimal shouldAmount;
    private BigDecimal shouldPrincipal;
    private BigDecimal shouldInterest;
    private BigDecimal fee1;
    private BigDecimal fee2;
    private BigDecimal fee3;
    private BigDecimal fee1Tax;
    private BigDecimal fee2Tax;
    private BigDecimal fee3Tax;
    private BigDecimal stampDuty;
    private BigDecimal shouldStampDuty;
    private BigDecimal reductionAmount;
    private BigDecimal reductionPrincipal;
    private BigDecimal reductionInterest;
    private BigDecimal reductionFee1;
    private BigDecimal reductionFee2;
    private BigDecimal reductionFee3;
    private BigDecimal reductionFee1Tax;
    private BigDecimal reductionFee2Tax;
    private BigDecimal reductionFee3Tax;
    private BigDecimal reductionStampDuty;

    public long getQuoteId() { return quoteId; }
    public void setQuoteId(long quoteId) { this.quoteId = quoteId; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public int getTermNo() { return termNo; }
    public void setTermNo(int termNo) { this.termNo = termNo; }
    public Long getValueDate() { return valueDate; }
    public void setValueDate(Long valueDate) { this.valueDate = valueDate; }
    public Long getDueDate() { return dueDate; }
    public void setDueDate(Long dueDate) { this.dueDate = dueDate; }
    public Long getGraceDate() { return graceDate; }
    public void setGraceDate(Long graceDate) { this.graceDate = graceDate; }
    public BigDecimal getSchdAmount() { return schdAmount; }
    public void setSchdAmount(BigDecimal schdAmount) { this.schdAmount = schdAmount; }
    public BigDecimal getSchdPrincipal() { return schdPrincipal; }
    public void setSchdPrincipal(BigDecimal schdPrincipal) { this.schdPrincipal = schdPrincipal; }
    public BigDecimal getShowLoanPrincipal() { return showLoanPrincipal; }
    public void setShowLoanPrincipal(BigDecimal showLoanPrincipal) { this.showLoanPrincipal = showLoanPrincipal; }
    public BigDecimal getSchdInterest() { return schdInterest; }
    public void setSchdInterest(BigDecimal schdInterest) { this.schdInterest = schdInterest; }
    public BigDecimal getShowInterest() { return showInterest; }
    public void setShowInterest(BigDecimal showInterest) { this.showInterest = showInterest; }
    public BigDecimal getShouldAmount() { return shouldAmount; }
    public void setShouldAmount(BigDecimal shouldAmount) { this.shouldAmount = shouldAmount; }
    public BigDecimal getShouldPrincipal() { return shouldPrincipal; }
    public void setShouldPrincipal(BigDecimal shouldPrincipal) { this.shouldPrincipal = shouldPrincipal; }
    public BigDecimal getShouldInterest() { return shouldInterest; }
    public void setShouldInterest(BigDecimal shouldInterest) { this.shouldInterest = shouldInterest; }
    public BigDecimal getFee1() { return fee1; }
    public void setFee1(BigDecimal fee1) { this.fee1 = fee1; }
    public BigDecimal getFee2() { return fee2; }
    public void setFee2(BigDecimal fee2) { this.fee2 = fee2; }
    public BigDecimal getFee3() { return fee3; }
    public void setFee3(BigDecimal fee3) { this.fee3 = fee3; }
    public BigDecimal getFee1Tax() { return fee1Tax; }
    public void setFee1Tax(BigDecimal fee1Tax) { this.fee1Tax = fee1Tax; }
    public BigDecimal getFee2Tax() { return fee2Tax; }
    public void setFee2Tax(BigDecimal fee2Tax) { this.fee2Tax = fee2Tax; }
    public BigDecimal getFee3Tax() { return fee3Tax; }
    public void setFee3Tax(BigDecimal fee3Tax) { this.fee3Tax = fee3Tax; }
    public BigDecimal getStampDuty() { return stampDuty; }
    public void setStampDuty(BigDecimal stampDuty) { this.stampDuty = stampDuty; }
    public BigDecimal getShouldStampDuty() { return shouldStampDuty; }
    public void setShouldStampDuty(BigDecimal shouldStampDuty) { this.shouldStampDuty = shouldStampDuty; }
    public BigDecimal getReductionAmount() { return reductionAmount; }
    public void setReductionAmount(BigDecimal reductionAmount) { this.reductionAmount = reductionAmount; }
    public BigDecimal getReductionPrincipal() { return reductionPrincipal; }
    public void setReductionPrincipal(BigDecimal reductionPrincipal) { this.reductionPrincipal = reductionPrincipal; }
    public BigDecimal getReductionInterest() { return reductionInterest; }
    public void setReductionInterest(BigDecimal reductionInterest) { this.reductionInterest = reductionInterest; }
    public BigDecimal getReductionFee1() { return reductionFee1; }
    public void setReductionFee1(BigDecimal reductionFee1) { this.reductionFee1 = reductionFee1; }
    public BigDecimal getReductionFee2() { return reductionFee2; }
    public void setReductionFee2(BigDecimal reductionFee2) { this.reductionFee2 = reductionFee2; }
    public BigDecimal getReductionFee3() { return reductionFee3; }
    public void setReductionFee3(BigDecimal reductionFee3) { this.reductionFee3 = reductionFee3; }
    public BigDecimal getReductionFee1Tax() { return reductionFee1Tax; }
    public void setReductionFee1Tax(BigDecimal reductionFee1Tax) { this.reductionFee1Tax = reductionFee1Tax; }
    public BigDecimal getReductionFee2Tax() { return reductionFee2Tax; }
    public void setReductionFee2Tax(BigDecimal reductionFee2Tax) { this.reductionFee2Tax = reductionFee2Tax; }
    public BigDecimal getReductionFee3Tax() { return reductionFee3Tax; }
    public void setReductionFee3Tax(BigDecimal reductionFee3Tax) { this.reductionFee3Tax = reductionFee3Tax; }
    public BigDecimal getReductionStampDuty() { return reductionStampDuty; }
    public void setReductionStampDuty(BigDecimal reductionStampDuty) { this.reductionStampDuty = reductionStampDuty; }
}
