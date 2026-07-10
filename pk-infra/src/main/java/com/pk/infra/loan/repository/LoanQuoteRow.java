package com.pk.infra.loan.repository;

import java.math.BigDecimal;
import java.time.Instant;

public class LoanQuoteRow {
    private long id;
    private String quoteNo;
    private long creditApplicationId;
    private String mobileNo;
    private Long productSnapshotId;
    private String applyId;
    private String creditApplyNo;
    private String userId;
    private String productCode;
    private String repayMethod;
    private BigDecimal applyAmt;
    private Integer loanTerm;
    private BigDecimal loanPrincipal;
    private BigDecimal showLoanPrincipal;
    private BigDecimal payAmount;
    private BigDecimal handFee;
    private BigDecimal schdAmount;
    private BigDecimal shouldAmount;
    private BigDecimal interest;
    private BigDecimal dayRate;
    private BigDecimal showDayRate;
    private Integer totalDays;
    private String fee1Name;
    private BigDecimal fee1;
    private String fee2Name;
    private BigDecimal fee2;
    private String fee3Name;
    private BigDecimal fee3;
    private BigDecimal tax;
    private String taxRatePercent;
    private BigDecimal ppnAmount;
    private BigDecimal shouldStampDuty;
    private BigDecimal shouldPrincipal;
    private BigDecimal shouldInterest;
    private BigDecimal shouldFee1;
    private BigDecimal shouldFee2;
    private BigDecimal shouldFee3;
    private BigDecimal shouldFee1Tax;
    private BigDecimal shouldFee2Tax;
    private BigDecimal shouldFee3Tax;
    private BigDecimal reductionAmount;
    private BigDecimal adReductionAmt;
    private BigDecimal adReductionRatio;
    private Long adReductionEndDate;
    private BigDecimal reductionStampDuty;
    private BigDecimal reductionPrincipal;
    private BigDecimal reductionInterest;
    private BigDecimal reductionFee1;
    private BigDecimal reductionFee2;
    private BigDecimal reductionFee3;
    private BigDecimal reductionFee1Tax;
    private BigDecimal reductionFee2Tax;
    private BigDecimal reductionFee3Tax;
    private String afterServiceFeeStatus;
    private Integer feePrepayType;
    private Long lendingDate;
    private Long firstRepayDate;
    private Long lastRepayDate;
    private Boolean unevenBillsFlag;
    private String lastLenderRequestJson;
    private String lastLenderResponseJson;
    private String rawResponseJson;
    private Instant quotedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getQuoteNo() { return quoteNo; }
    public void setQuoteNo(String quoteNo) { this.quoteNo = quoteNo; }
    public long getCreditApplicationId() { return creditApplicationId; }
    public void setCreditApplicationId(long creditApplicationId) { this.creditApplicationId = creditApplicationId; }
    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }
    public Long getProductSnapshotId() { return productSnapshotId; }
    public void setProductSnapshotId(Long productSnapshotId) { this.productSnapshotId = productSnapshotId; }
    public String getApplyId() { return applyId; }
    public void setApplyId(String applyId) { this.applyId = applyId; }
    public String getCreditApplyNo() { return creditApplyNo; }
    public void setCreditApplyNo(String creditApplyNo) { this.creditApplyNo = creditApplyNo; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getRepayMethod() { return repayMethod; }
    public void setRepayMethod(String repayMethod) { this.repayMethod = repayMethod; }
    public BigDecimal getApplyAmt() { return applyAmt; }
    public void setApplyAmt(BigDecimal applyAmt) { this.applyAmt = applyAmt; }
    public Integer getLoanTerm() { return loanTerm; }
    public void setLoanTerm(Integer loanTerm) { this.loanTerm = loanTerm; }
    public BigDecimal getLoanPrincipal() { return loanPrincipal; }
    public void setLoanPrincipal(BigDecimal loanPrincipal) { this.loanPrincipal = loanPrincipal; }
    public BigDecimal getShowLoanPrincipal() { return showLoanPrincipal; }
    public void setShowLoanPrincipal(BigDecimal showLoanPrincipal) { this.showLoanPrincipal = showLoanPrincipal; }
    public BigDecimal getPayAmount() { return payAmount; }
    public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
    public BigDecimal getHandFee() { return handFee; }
    public void setHandFee(BigDecimal handFee) { this.handFee = handFee; }
    public BigDecimal getSchdAmount() { return schdAmount; }
    public void setSchdAmount(BigDecimal schdAmount) { this.schdAmount = schdAmount; }
    public BigDecimal getShouldAmount() { return shouldAmount; }
    public void setShouldAmount(BigDecimal shouldAmount) { this.shouldAmount = shouldAmount; }
    public BigDecimal getInterest() { return interest; }
    public void setInterest(BigDecimal interest) { this.interest = interest; }
    public BigDecimal getDayRate() { return dayRate; }
    public void setDayRate(BigDecimal dayRate) { this.dayRate = dayRate; }
    public BigDecimal getShowDayRate() { return showDayRate; }
    public void setShowDayRate(BigDecimal showDayRate) { this.showDayRate = showDayRate; }
    public Integer getTotalDays() { return totalDays; }
    public void setTotalDays(Integer totalDays) { this.totalDays = totalDays; }
    public String getFee1Name() { return fee1Name; }
    public void setFee1Name(String fee1Name) { this.fee1Name = fee1Name; }
    public BigDecimal getFee1() { return fee1; }
    public void setFee1(BigDecimal fee1) { this.fee1 = fee1; }
    public String getFee2Name() { return fee2Name; }
    public void setFee2Name(String fee2Name) { this.fee2Name = fee2Name; }
    public BigDecimal getFee2() { return fee2; }
    public void setFee2(BigDecimal fee2) { this.fee2 = fee2; }
    public String getFee3Name() { return fee3Name; }
    public void setFee3Name(String fee3Name) { this.fee3Name = fee3Name; }
    public BigDecimal getFee3() { return fee3; }
    public void setFee3(BigDecimal fee3) { this.fee3 = fee3; }
    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }
    public String getTaxRatePercent() { return taxRatePercent; }
    public void setTaxRatePercent(String taxRatePercent) { this.taxRatePercent = taxRatePercent; }
    public BigDecimal getPpnAmount() { return ppnAmount; }
    public void setPpnAmount(BigDecimal ppnAmount) { this.ppnAmount = ppnAmount; }
    public BigDecimal getShouldStampDuty() { return shouldStampDuty; }
    public void setShouldStampDuty(BigDecimal shouldStampDuty) { this.shouldStampDuty = shouldStampDuty; }
    public BigDecimal getShouldPrincipal() { return shouldPrincipal; }
    public void setShouldPrincipal(BigDecimal shouldPrincipal) { this.shouldPrincipal = shouldPrincipal; }
    public BigDecimal getShouldInterest() { return shouldInterest; }
    public void setShouldInterest(BigDecimal shouldInterest) { this.shouldInterest = shouldInterest; }
    public BigDecimal getShouldFee1() { return shouldFee1; }
    public void setShouldFee1(BigDecimal shouldFee1) { this.shouldFee1 = shouldFee1; }
    public BigDecimal getShouldFee2() { return shouldFee2; }
    public void setShouldFee2(BigDecimal shouldFee2) { this.shouldFee2 = shouldFee2; }
    public BigDecimal getShouldFee3() { return shouldFee3; }
    public void setShouldFee3(BigDecimal shouldFee3) { this.shouldFee3 = shouldFee3; }
    public BigDecimal getShouldFee1Tax() { return shouldFee1Tax; }
    public void setShouldFee1Tax(BigDecimal shouldFee1Tax) { this.shouldFee1Tax = shouldFee1Tax; }
    public BigDecimal getShouldFee2Tax() { return shouldFee2Tax; }
    public void setShouldFee2Tax(BigDecimal shouldFee2Tax) { this.shouldFee2Tax = shouldFee2Tax; }
    public BigDecimal getShouldFee3Tax() { return shouldFee3Tax; }
    public void setShouldFee3Tax(BigDecimal shouldFee3Tax) { this.shouldFee3Tax = shouldFee3Tax; }
    public BigDecimal getReductionAmount() { return reductionAmount; }
    public void setReductionAmount(BigDecimal reductionAmount) { this.reductionAmount = reductionAmount; }
    public BigDecimal getAdReductionAmt() { return adReductionAmt; }
    public void setAdReductionAmt(BigDecimal adReductionAmt) { this.adReductionAmt = adReductionAmt; }
    public BigDecimal getAdReductionRatio() { return adReductionRatio; }
    public void setAdReductionRatio(BigDecimal adReductionRatio) { this.adReductionRatio = adReductionRatio; }
    public Long getAdReductionEndDate() { return adReductionEndDate; }
    public void setAdReductionEndDate(Long adReductionEndDate) { this.adReductionEndDate = adReductionEndDate; }
    public BigDecimal getReductionStampDuty() { return reductionStampDuty; }
    public void setReductionStampDuty(BigDecimal reductionStampDuty) { this.reductionStampDuty = reductionStampDuty; }
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
    public String getAfterServiceFeeStatus() { return afterServiceFeeStatus; }
    public void setAfterServiceFeeStatus(String afterServiceFeeStatus) { this.afterServiceFeeStatus = afterServiceFeeStatus; }
    public Integer getFeePrepayType() { return feePrepayType; }
    public void setFeePrepayType(Integer feePrepayType) { this.feePrepayType = feePrepayType; }
    public Long getLendingDate() { return lendingDate; }
    public void setLendingDate(Long lendingDate) { this.lendingDate = lendingDate; }
    public Long getFirstRepayDate() { return firstRepayDate; }
    public void setFirstRepayDate(Long firstRepayDate) { this.firstRepayDate = firstRepayDate; }
    public Long getLastRepayDate() { return lastRepayDate; }
    public void setLastRepayDate(Long lastRepayDate) { this.lastRepayDate = lastRepayDate; }
    public Boolean getUnevenBillsFlag() { return unevenBillsFlag; }
    public void setUnevenBillsFlag(Boolean unevenBillsFlag) { this.unevenBillsFlag = unevenBillsFlag; }
    public String getLastLenderRequestJson() { return lastLenderRequestJson; }
    public void setLastLenderRequestJson(String lastLenderRequestJson) { this.lastLenderRequestJson = lastLenderRequestJson; }
    public String getLastLenderResponseJson() { return lastLenderResponseJson; }
    public void setLastLenderResponseJson(String lastLenderResponseJson) { this.lastLenderResponseJson = lastLenderResponseJson; }
    public String getRawResponseJson() { return rawResponseJson; }
    public void setRawResponseJson(String rawResponseJson) { this.rawResponseJson = rawResponseJson; }
    public Instant getQuotedAt() { return quotedAt; }
    public void setQuotedAt(Instant quotedAt) { this.quotedAt = quotedAt; }
}
