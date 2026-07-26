package com.pk.infra.repay.repository;

import java.math.BigDecimal;

public class RepaymentTrialInsertParam {
    private long id;
    private String trialNo;
    private long userId;
    private String trialType;
    private Integer totalBillCount;
    private BigDecimal totalShouldAmount;
    private BigDecimal totalReductionAmount;
    private BigDecimal totalPaidAmount;
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
    private Long externalInteractionId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTrialNo() {
        return trialNo;
    }

    public void setTrialNo(String trialNo) {
        this.trialNo = trialNo;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTrialType() {
        return trialType;
    }

    public void setTrialType(String trialType) {
        this.trialType = trialType;
    }

    public Integer getTotalBillCount() {
        return totalBillCount;
    }

    public void setTotalBillCount(Integer totalBillCount) {
        this.totalBillCount = totalBillCount;
    }

    public BigDecimal getTotalShouldAmount() {
        return totalShouldAmount;
    }

    public void setTotalShouldAmount(BigDecimal totalShouldAmount) {
        this.totalShouldAmount = totalShouldAmount;
    }

    public BigDecimal getTotalReductionAmount() {
        return totalReductionAmount;
    }

    public void setTotalReductionAmount(BigDecimal totalReductionAmount) {
        this.totalReductionAmount = totalReductionAmount;
    }

    public BigDecimal getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(BigDecimal totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
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

    public Long getExternalInteractionId() {
        return externalInteractionId;
    }

    public void setExternalInteractionId(Long externalInteractionId) {
        this.externalInteractionId = externalInteractionId;
    }
}
