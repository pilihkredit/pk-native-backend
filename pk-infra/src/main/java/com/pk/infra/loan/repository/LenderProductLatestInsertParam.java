package com.pk.infra.loan.repository;

import java.math.BigDecimal;

public class LenderProductLatestInsertParam {
    private long productListLatestId;
    private String mobileNo;
    private String productCode;
    private String productName;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String comprehensiveRateUnit;
    private BigDecimal comprehensiveRate;
    private int sortOrder;
    private Long id;

    public long getProductListLatestId() {
        return productListLatestId;
    }

    public void setProductListLatestId(long productListLatestId) {
        this.productListLatestId = productListLatestId;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
