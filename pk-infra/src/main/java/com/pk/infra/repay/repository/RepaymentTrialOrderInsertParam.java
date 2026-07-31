package com.pk.infra.repay.repository;

import java.math.BigDecimal;
import java.time.Instant;

public class RepaymentTrialOrderInsertParam {
    private long id;
    private long trialId;
    private long loanApplicationId;
    private String loanApplyId;
    private String loanApplyNo;
    private String billNo;
    private boolean settle;
    private String termNosJson;
    private BigDecimal shouldAmount;
    private String billStatus;
    private String name;
    private String userId;
    private String advSetteFlag;
    private String currency;
    private BigDecimal applyAmt;
    private Instant applyTime;
    private Instant auditTime;
    private Instant loanTime;
    private Integer loanDays;
    private Integer repayMethodType;
    private BigDecimal schdAmount;
    private BigDecimal schdStampDuty;
    private BigDecimal schdPrincipal;
    private BigDecimal schdInterest;
    private BigDecimal schdFee1;
    private BigDecimal schdFee2;
    private BigDecimal schdFee3;
    private BigDecimal schdFee1Tax;
    private BigDecimal schdFee2Tax;
    private BigDecimal schdFee3Tax;
    private BigDecimal prePenInterest;
    private BigDecimal penInterest;
    private BigDecimal initLateFee;
    private BigDecimal shouldStampDuty;
    private BigDecimal shouldPrincipal;
    private BigDecimal shouldInterest;
    private String fee1Name;
    private BigDecimal shouldFee1;
    private String fee2Name;
    private BigDecimal shouldFee2;
    private String fee3Name;
    private BigDecimal shouldFee3;
    private BigDecimal shouldFee1Tax;
    private BigDecimal shouldFee2Tax;
    private BigDecimal shouldFee3Tax;
    private BigDecimal shouldFee;
    private BigDecimal shouldPenInterest;
    private BigDecimal shouldInitLateFee;
    private BigDecimal shouldAdvSettleFee;
    private BigDecimal shouldPenalty;
    private BigDecimal reductionAmount;
    private BigDecimal reductionStampDuty;
    private BigDecimal reductionPrincipal;
    private BigDecimal reductionInterest;
    private BigDecimal reductionFee1;
    private BigDecimal reductionFee2;
    private BigDecimal reductionFee3;
    private BigDecimal reductionFee1Tax;
    private BigDecimal reductionFee2Tax;
    private BigDecimal reductionFee3Tax;
    private BigDecimal reductionPenInterest;
    private BigDecimal reductionInitLateFee;
    private BigDecimal couponAmount;
    private String partRepayFlag;
    private BigDecimal paidAmount;
    private Long couponId;
    private String couponType;
    private String couponName;
    private Long userValideDisTime;
    private Integer termNo;
    private Instant termDueDate;
    private String defaultVaNo;
    private String defaultVaBankCode;
    private String defaultVaBankName;
    private Integer defaultVaBankType;
    private String defaultVaIcon;
    private Boolean defaultVaDefaultFlag;
    private Boolean defaultVaDisabled;
    private Boolean defaultVaShow;
    private String spareVaNo;
    private String spareVaBankCode;
    private String spareVaBankName;
    private Integer spareVaBankType;
    private String spareVaIcon;
    private Boolean spareVaDefaultFlag;
    private Boolean spareVaDisabled;
    private Boolean spareVaShow;
    private String disabledDefaultVaNo;
    private String disabledDefaultVaBankCode;
    private String disabledDefaultVaBankName;
    private Integer disabledDefaultVaBankType;
    private String disabledDefaultVaIcon;
    private Boolean disabledDefaultVaDefaultFlag;
    private Boolean disabledDefaultVaDisabled;
    private Boolean disabledDefaultVaShow;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTrialId() {
        return trialId;
    }

    public void setTrialId(long trialId) {
        this.trialId = trialId;
    }

    public long getLoanApplicationId() {
        return loanApplicationId;
    }

    public void setLoanApplicationId(long loanApplicationId) {
        this.loanApplicationId = loanApplicationId;
    }

    public String getLoanApplyId() {
        return loanApplyId;
    }

    public void setLoanApplyId(String loanApplyId) {
        this.loanApplyId = loanApplyId;
    }

    public String getLoanApplyNo() {
        return loanApplyNo;
    }

    public void setLoanApplyNo(String loanApplyNo) {
        this.loanApplyNo = loanApplyNo;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public boolean isSettle() {
        return settle;
    }

    public void setSettle(boolean settle) {
        this.settle = settle;
    }

    public String getTermNosJson() {
        return termNosJson;
    }

    public void setTermNosJson(String termNosJson) {
        this.termNosJson = termNosJson;
    }

    public BigDecimal getShouldAmount() {
        return shouldAmount;
    }

    public void setShouldAmount(BigDecimal shouldAmount) {
        this.shouldAmount = shouldAmount;
    }

    public String getBillStatus() {
        return billStatus;
    }

    public void setBillStatus(String billStatus) {
        this.billStatus = billStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAdvSetteFlag() {
        return advSetteFlag;
    }

    public void setAdvSetteFlag(String advSetteFlag) {
        this.advSetteFlag = advSetteFlag;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getApplyAmt() {
        return applyAmt;
    }

    public void setApplyAmt(BigDecimal applyAmt) {
        this.applyAmt = applyAmt;
    }

    public Instant getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Instant applyTime) {
        this.applyTime = applyTime;
    }

    public Instant getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(Instant auditTime) {
        this.auditTime = auditTime;
    }

    public Instant getLoanTime() {
        return loanTime;
    }

    public void setLoanTime(Instant loanTime) {
        this.loanTime = loanTime;
    }

    public Integer getLoanDays() {
        return loanDays;
    }

    public void setLoanDays(Integer loanDays) {
        this.loanDays = loanDays;
    }

    public Integer getRepayMethodType() {
        return repayMethodType;
    }

    public void setRepayMethodType(Integer repayMethodType) {
        this.repayMethodType = repayMethodType;
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

    public String getFee1Name() {
        return fee1Name;
    }

    public void setFee1Name(String fee1Name) {
        this.fee1Name = fee1Name;
    }

    public BigDecimal getShouldFee1() {
        return shouldFee1;
    }

    public void setShouldFee1(BigDecimal shouldFee1) {
        this.shouldFee1 = shouldFee1;
    }

    public String getFee2Name() {
        return fee2Name;
    }

    public void setFee2Name(String fee2Name) {
        this.fee2Name = fee2Name;
    }

    public BigDecimal getShouldFee2() {
        return shouldFee2;
    }

    public void setShouldFee2(BigDecimal shouldFee2) {
        this.shouldFee2 = shouldFee2;
    }

    public String getFee3Name() {
        return fee3Name;
    }

    public void setFee3Name(String fee3Name) {
        this.fee3Name = fee3Name;
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

    public BigDecimal getShouldFee() {
        return shouldFee;
    }

    public void setShouldFee(BigDecimal shouldFee) {
        this.shouldFee = shouldFee;
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

    public BigDecimal getShouldAdvSettleFee() {
        return shouldAdvSettleFee;
    }

    public void setShouldAdvSettleFee(BigDecimal shouldAdvSettleFee) {
        this.shouldAdvSettleFee = shouldAdvSettleFee;
    }

    public BigDecimal getShouldPenalty() {
        return shouldPenalty;
    }

    public void setShouldPenalty(BigDecimal shouldPenalty) {
        this.shouldPenalty = shouldPenalty;
    }

    public BigDecimal getReductionAmount() {
        return reductionAmount;
    }

    public void setReductionAmount(BigDecimal reductionAmount) {
        this.reductionAmount = reductionAmount;
    }

    public BigDecimal getReductionStampDuty() {
        return reductionStampDuty;
    }

    public void setReductionStampDuty(BigDecimal reductionStampDuty) {
        this.reductionStampDuty = reductionStampDuty;
    }

    public BigDecimal getReductionPrincipal() {
        return reductionPrincipal;
    }

    public void setReductionPrincipal(BigDecimal reductionPrincipal) {
        this.reductionPrincipal = reductionPrincipal;
    }

    public BigDecimal getReductionInterest() {
        return reductionInterest;
    }

    public void setReductionInterest(BigDecimal reductionInterest) {
        this.reductionInterest = reductionInterest;
    }

    public BigDecimal getReductionFee1() {
        return reductionFee1;
    }

    public void setReductionFee1(BigDecimal reductionFee1) {
        this.reductionFee1 = reductionFee1;
    }

    public BigDecimal getReductionFee2() {
        return reductionFee2;
    }

    public void setReductionFee2(BigDecimal reductionFee2) {
        this.reductionFee2 = reductionFee2;
    }

    public BigDecimal getReductionFee3() {
        return reductionFee3;
    }

    public void setReductionFee3(BigDecimal reductionFee3) {
        this.reductionFee3 = reductionFee3;
    }

    public BigDecimal getReductionFee1Tax() {
        return reductionFee1Tax;
    }

    public void setReductionFee1Tax(BigDecimal reductionFee1Tax) {
        this.reductionFee1Tax = reductionFee1Tax;
    }

    public BigDecimal getReductionFee2Tax() {
        return reductionFee2Tax;
    }

    public void setReductionFee2Tax(BigDecimal reductionFee2Tax) {
        this.reductionFee2Tax = reductionFee2Tax;
    }

    public BigDecimal getReductionFee3Tax() {
        return reductionFee3Tax;
    }

    public void setReductionFee3Tax(BigDecimal reductionFee3Tax) {
        this.reductionFee3Tax = reductionFee3Tax;
    }

    public BigDecimal getReductionPenInterest() {
        return reductionPenInterest;
    }

    public void setReductionPenInterest(BigDecimal reductionPenInterest) {
        this.reductionPenInterest = reductionPenInterest;
    }

    public BigDecimal getReductionInitLateFee() {
        return reductionInitLateFee;
    }

    public void setReductionInitLateFee(BigDecimal reductionInitLateFee) {
        this.reductionInitLateFee = reductionInitLateFee;
    }

    public BigDecimal getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(BigDecimal couponAmount) {
        this.couponAmount = couponAmount;
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

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public String getCouponType() {
        return couponType;
    }

    public void setCouponType(String couponType) {
        this.couponType = couponType;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public Long getUserValideDisTime() {
        return userValideDisTime;
    }

    public void setUserValideDisTime(Long userValideDisTime) {
        this.userValideDisTime = userValideDisTime;
    }

    public Integer getTermNo() {
        return termNo;
    }

    public void setTermNo(Integer termNo) {
        this.termNo = termNo;
    }

    public Instant getTermDueDate() {
        return termDueDate;
    }

    public void setTermDueDate(Instant termDueDate) {
        this.termDueDate = termDueDate;
    }

    public String getDefaultVaNo() {
        return defaultVaNo;
    }

    public void setDefaultVaNo(String defaultVaNo) {
        this.defaultVaNo = defaultVaNo;
    }

    public String getDefaultVaBankCode() {
        return defaultVaBankCode;
    }

    public void setDefaultVaBankCode(String defaultVaBankCode) {
        this.defaultVaBankCode = defaultVaBankCode;
    }

    public String getDefaultVaBankName() {
        return defaultVaBankName;
    }

    public void setDefaultVaBankName(String defaultVaBankName) {
        this.defaultVaBankName = defaultVaBankName;
    }

    public Integer getDefaultVaBankType() {
        return defaultVaBankType;
    }

    public void setDefaultVaBankType(Integer defaultVaBankType) {
        this.defaultVaBankType = defaultVaBankType;
    }

    public String getDefaultVaIcon() {
        return defaultVaIcon;
    }

    public void setDefaultVaIcon(String defaultVaIcon) {
        this.defaultVaIcon = defaultVaIcon;
    }

    public Boolean getDefaultVaDefaultFlag() {
        return defaultVaDefaultFlag;
    }

    public void setDefaultVaDefaultFlag(Boolean defaultVaDefaultFlag) {
        this.defaultVaDefaultFlag = defaultVaDefaultFlag;
    }

    public Boolean getDefaultVaDisabled() {
        return defaultVaDisabled;
    }

    public void setDefaultVaDisabled(Boolean defaultVaDisabled) {
        this.defaultVaDisabled = defaultVaDisabled;
    }

    public Boolean getDefaultVaShow() {
        return defaultVaShow;
    }

    public void setDefaultVaShow(Boolean defaultVaShow) {
        this.defaultVaShow = defaultVaShow;
    }

    public String getSpareVaNo() {
        return spareVaNo;
    }

    public void setSpareVaNo(String spareVaNo) {
        this.spareVaNo = spareVaNo;
    }

    public String getSpareVaBankCode() {
        return spareVaBankCode;
    }

    public void setSpareVaBankCode(String spareVaBankCode) {
        this.spareVaBankCode = spareVaBankCode;
    }

    public String getSpareVaBankName() {
        return spareVaBankName;
    }

    public void setSpareVaBankName(String spareVaBankName) {
        this.spareVaBankName = spareVaBankName;
    }

    public Integer getSpareVaBankType() {
        return spareVaBankType;
    }

    public void setSpareVaBankType(Integer spareVaBankType) {
        this.spareVaBankType = spareVaBankType;
    }

    public String getSpareVaIcon() {
        return spareVaIcon;
    }

    public void setSpareVaIcon(String spareVaIcon) {
        this.spareVaIcon = spareVaIcon;
    }

    public Boolean getSpareVaDefaultFlag() {
        return spareVaDefaultFlag;
    }

    public void setSpareVaDefaultFlag(Boolean spareVaDefaultFlag) {
        this.spareVaDefaultFlag = spareVaDefaultFlag;
    }

    public Boolean getSpareVaDisabled() {
        return spareVaDisabled;
    }

    public void setSpareVaDisabled(Boolean spareVaDisabled) {
        this.spareVaDisabled = spareVaDisabled;
    }

    public Boolean getSpareVaShow() {
        return spareVaShow;
    }

    public void setSpareVaShow(Boolean spareVaShow) {
        this.spareVaShow = spareVaShow;
    }

    public String getDisabledDefaultVaNo() {
        return disabledDefaultVaNo;
    }

    public void setDisabledDefaultVaNo(String disabledDefaultVaNo) {
        this.disabledDefaultVaNo = disabledDefaultVaNo;
    }

    public String getDisabledDefaultVaBankCode() {
        return disabledDefaultVaBankCode;
    }

    public void setDisabledDefaultVaBankCode(String disabledDefaultVaBankCode) {
        this.disabledDefaultVaBankCode = disabledDefaultVaBankCode;
    }

    public String getDisabledDefaultVaBankName() {
        return disabledDefaultVaBankName;
    }

    public void setDisabledDefaultVaBankName(String disabledDefaultVaBankName) {
        this.disabledDefaultVaBankName = disabledDefaultVaBankName;
    }

    public Integer getDisabledDefaultVaBankType() {
        return disabledDefaultVaBankType;
    }

    public void setDisabledDefaultVaBankType(Integer disabledDefaultVaBankType) {
        this.disabledDefaultVaBankType = disabledDefaultVaBankType;
    }

    public String getDisabledDefaultVaIcon() {
        return disabledDefaultVaIcon;
    }

    public void setDisabledDefaultVaIcon(String disabledDefaultVaIcon) {
        this.disabledDefaultVaIcon = disabledDefaultVaIcon;
    }

    public Boolean getDisabledDefaultVaDefaultFlag() {
        return disabledDefaultVaDefaultFlag;
    }

    public void setDisabledDefaultVaDefaultFlag(Boolean disabledDefaultVaDefaultFlag) {
        this.disabledDefaultVaDefaultFlag = disabledDefaultVaDefaultFlag;
    }

    public Boolean getDisabledDefaultVaDisabled() {
        return disabledDefaultVaDisabled;
    }

    public void setDisabledDefaultVaDisabled(Boolean disabledDefaultVaDisabled) {
        this.disabledDefaultVaDisabled = disabledDefaultVaDisabled;
    }

    public Boolean getDisabledDefaultVaShow() {
        return disabledDefaultVaShow;
    }

    public void setDisabledDefaultVaShow(Boolean disabledDefaultVaShow) {
        this.disabledDefaultVaShow = disabledDefaultVaShow;
    }
}
