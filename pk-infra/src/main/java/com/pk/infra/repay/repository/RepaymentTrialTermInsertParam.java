package com.pk.infra.repay.repository;

import java.math.BigDecimal;
import java.time.Instant;

public class RepaymentTrialTermInsertParam {
    private long id;
    private long trialOrderId;
    private String billNo;
    private String loanApplyNo;
    private int termNo;
    private String advSetteFlag;
    private BigDecimal schdAmount;
    private BigDecimal schdStampDuty;
    private BigDecimal schdPrincipal;
    private BigDecimal schdInterest;
    private BigDecimal schdAllFee;
    private BigDecimal schdAllTaxFee;
    private BigDecimal schdFee1;
    private BigDecimal schdFee2;
    private BigDecimal schdFee3;
    private BigDecimal schdFee1Tax;
    private BigDecimal schdFee2Tax;
    private BigDecimal schdFee3Tax;
    private BigDecimal prePenInterest;
    private BigDecimal penInterest;
    private BigDecimal initLateFee;
    private Instant dueDate;
    private Integer overdueDays;
    private Instant graceDate;
    private String termStatus;
    private Long daysOfDueDate;
    private BigDecimal shouldAmount;
    private BigDecimal shouldStampDuty;
    private BigDecimal shouldPrincipal;
    private BigDecimal shouldInterest;
    private BigDecimal shouldFee1;
    private BigDecimal shouldFee2;
    private BigDecimal shouldFee3;
    private BigDecimal shouldFee1Tax;
    private BigDecimal shouldFee2Tax;
    private BigDecimal shouldFee3Tax;
    private BigDecimal shouldPenInterest;
    private BigDecimal shouldInitLateFee;
    private String partRepayFlag;
    private BigDecimal paidAmount;
    private BigDecimal couponDiscount;
    private BigDecimal reductionAmount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTrialOrderId() {
        return trialOrderId;
    }

    public void setTrialOrderId(long trialOrderId) {
        this.trialOrderId = trialOrderId;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public String getLoanApplyNo() {
        return loanApplyNo;
    }

    public void setLoanApplyNo(String loanApplyNo) {
        this.loanApplyNo = loanApplyNo;
    }

    public int getTermNo() {
        return termNo;
    }

    public void setTermNo(int termNo) {
        this.termNo = termNo;
    }

    public String getAdvSetteFlag() {
        return advSetteFlag;
    }

    public void setAdvSetteFlag(String advSetteFlag) {
        this.advSetteFlag = advSetteFlag;
    }

    public BigDecimal getSchdAmount() {
        return schdAmount;
    }

    public void setSchdAmount(BigDecimal schdAmount) {
        this.schdAmount = schdAmount;
    }

    public BigDecimal getSchdStampDuty() {
        return schdStampDuty;
    }

    public void setSchdStampDuty(BigDecimal schdStampDuty) {
        this.schdStampDuty = schdStampDuty;
    }

    public BigDecimal getSchdPrincipal() {
        return schdPrincipal;
    }

    public void setSchdPrincipal(BigDecimal schdPrincipal) {
        this.schdPrincipal = schdPrincipal;
    }

    public BigDecimal getSchdInterest() {
        return schdInterest;
    }

    public void setSchdInterest(BigDecimal schdInterest) {
        this.schdInterest = schdInterest;
    }

    public BigDecimal getSchdAllFee() {
        return schdAllFee;
    }

    public void setSchdAllFee(BigDecimal schdAllFee) {
        this.schdAllFee = schdAllFee;
    }

    public BigDecimal getSchdAllTaxFee() {
        return schdAllTaxFee;
    }

    public void setSchdAllTaxFee(BigDecimal schdAllTaxFee) {
        this.schdAllTaxFee = schdAllTaxFee;
    }

    public BigDecimal getSchdFee1() {
        return schdFee1;
    }

    public void setSchdFee1(BigDecimal schdFee1) {
        this.schdFee1 = schdFee1;
    }

    public BigDecimal getSchdFee2() {
        return schdFee2;
    }

    public void setSchdFee2(BigDecimal schdFee2) {
        this.schdFee2 = schdFee2;
    }

    public BigDecimal getSchdFee3() {
        return schdFee3;
    }

    public void setSchdFee3(BigDecimal schdFee3) {
        this.schdFee3 = schdFee3;
    }

    public BigDecimal getSchdFee1Tax() {
        return schdFee1Tax;
    }

    public void setSchdFee1Tax(BigDecimal schdFee1Tax) {
        this.schdFee1Tax = schdFee1Tax;
    }

    public BigDecimal getSchdFee2Tax() {
        return schdFee2Tax;
    }

    public void setSchdFee2Tax(BigDecimal schdFee2Tax) {
        this.schdFee2Tax = schdFee2Tax;
    }

    public BigDecimal getSchdFee3Tax() {
        return schdFee3Tax;
    }

    public void setSchdFee3Tax(BigDecimal schdFee3Tax) {
        this.schdFee3Tax = schdFee3Tax;
    }

    public BigDecimal getPrePenInterest() {
        return prePenInterest;
    }

    public void setPrePenInterest(BigDecimal prePenInterest) {
        this.prePenInterest = prePenInterest;
    }

    public BigDecimal getPenInterest() {
        return penInterest;
    }

    public void setPenInterest(BigDecimal penInterest) {
        this.penInterest = penInterest;
    }

    public BigDecimal getInitLateFee() {
        return initLateFee;
    }

    public void setInitLateFee(BigDecimal initLateFee) {
        this.initLateFee = initLateFee;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }

    public Instant getGraceDate() {
        return graceDate;
    }

    public void setGraceDate(Instant graceDate) {
        this.graceDate = graceDate;
    }

    public String getTermStatus() {
        return termStatus;
    }

    public void setTermStatus(String termStatus) {
        this.termStatus = termStatus;
    }

    public Long getDaysOfDueDate() {
        return daysOfDueDate;
    }

    public void setDaysOfDueDate(Long daysOfDueDate) {
        this.daysOfDueDate = daysOfDueDate;
    }

    public BigDecimal getShouldAmount() {
        return shouldAmount;
    }

    public void setShouldAmount(BigDecimal shouldAmount) {
        this.shouldAmount = shouldAmount;
    }

    public BigDecimal getShouldStampDuty() {
        return shouldStampDuty;
    }

    public void setShouldStampDuty(BigDecimal shouldStampDuty) {
        this.shouldStampDuty = shouldStampDuty;
    }

    public BigDecimal getShouldPrincipal() {
        return shouldPrincipal;
    }

    public void setShouldPrincipal(BigDecimal shouldPrincipal) {
        this.shouldPrincipal = shouldPrincipal;
    }

    public BigDecimal getShouldInterest() {
        return shouldInterest;
    }

    public void setShouldInterest(BigDecimal shouldInterest) {
        this.shouldInterest = shouldInterest;
    }

    public BigDecimal getShouldFee1() {
        return shouldFee1;
    }

    public void setShouldFee1(BigDecimal shouldFee1) {
        this.shouldFee1 = shouldFee1;
    }

    public BigDecimal getShouldFee2() {
        return shouldFee2;
    }

    public void setShouldFee2(BigDecimal shouldFee2) {
        this.shouldFee2 = shouldFee2;
    }

    public BigDecimal getShouldFee3() {
        return shouldFee3;
    }

    public void setShouldFee3(BigDecimal shouldFee3) {
        this.shouldFee3 = shouldFee3;
    }

    public BigDecimal getShouldFee1Tax() {
        return shouldFee1Tax;
    }

    public void setShouldFee1Tax(BigDecimal shouldFee1Tax) {
        this.shouldFee1Tax = shouldFee1Tax;
    }

    public BigDecimal getShouldFee2Tax() {
        return shouldFee2Tax;
    }

    public void setShouldFee2Tax(BigDecimal shouldFee2Tax) {
        this.shouldFee2Tax = shouldFee2Tax;
    }

    public BigDecimal getShouldFee3Tax() {
        return shouldFee3Tax;
    }

    public void setShouldFee3Tax(BigDecimal shouldFee3Tax) {
        this.shouldFee3Tax = shouldFee3Tax;
    }

    public BigDecimal getShouldPenInterest() {
        return shouldPenInterest;
    }

    public void setShouldPenInterest(BigDecimal shouldPenInterest) {
        this.shouldPenInterest = shouldPenInterest;
    }

    public BigDecimal getShouldInitLateFee() {
        return shouldInitLateFee;
    }

    public void setShouldInitLateFee(BigDecimal shouldInitLateFee) {
        this.shouldInitLateFee = shouldInitLateFee;
    }

    public String getPartRepayFlag() {
        return partRepayFlag;
    }

    public void setPartRepayFlag(String partRepayFlag) {
        this.partRepayFlag = partRepayFlag;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public BigDecimal getReductionAmount() {
        return reductionAmount;
    }

    public void setReductionAmount(BigDecimal reductionAmount) {
        this.reductionAmount = reductionAmount;
    }
}
