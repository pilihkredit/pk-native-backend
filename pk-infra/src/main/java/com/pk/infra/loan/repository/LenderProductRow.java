package com.pk.infra.loan.repository;

import java.math.BigDecimal;

public class LenderProductRow {
    private long id;
    private long productListId;
    private String mobileNo;
    private String productCode;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String comprehensiveRateUnit;
    private BigDecimal comprehensiveRate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductListId() {
        return productListId;
    }

    public void setProductListId(long productListId) {
        this.productListId = productListId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getComprehensiveRateUnit() {
        return comprehensiveRateUnit;
    }

    public void setComprehensiveRateUnit(String comprehensiveRateUnit) {
        this.comprehensiveRateUnit = comprehensiveRateUnit;
    }

    public BigDecimal getComprehensiveRate() {
        return comprehensiveRate;
    }

    public void setComprehensiveRate(BigDecimal comprehensiveRate) {
        this.comprehensiveRate = comprehensiveRate;
    }
}
