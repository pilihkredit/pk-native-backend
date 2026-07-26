package com.pk.infra.loan.repository;

import java.math.BigDecimal;

public class LenderProductInsertParam {
    private Long id;
    private long productListId;
    private long userId;
    private String productCode;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String comprehensiveRateUnit;
    private BigDecimal comprehensiveRate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProductListId() {
        return productListId;
    }

    public void setProductListId(long productListId) {
        this.productListId = productListId;
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

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
