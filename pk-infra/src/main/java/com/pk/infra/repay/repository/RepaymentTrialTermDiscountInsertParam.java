package com.pk.infra.repay.repository;

import java.math.BigDecimal;

public class RepaymentTrialTermDiscountInsertParam {
    private long trialTermId;
    private BigDecimal reductionAllAmt;
    private String reductionType;
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
    private Long couponId;
    private String couponType;

    public long getTrialTermId() {
        return trialTermId;
    }

    public void setTrialTermId(long trialTermId) {
        this.trialTermId = trialTermId;
    }

    public BigDecimal getReductionAllAmt() {
        return reductionAllAmt;
    }

    public void setReductionAllAmt(BigDecimal reductionAllAmt) {
        this.reductionAllAmt = reductionAllAmt;
    }

    public String getReductionType() {
        return reductionType;
    }

    public void setReductionType(String reductionType) {
        this.reductionType = reductionType;
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
}
