package com.pk.infra.loan.repository;

import com.pk.core.loan.port.LoanApplicationRepository.LoanApplicationInsert;
import java.math.BigDecimal;

public class LoanApplicationInsertParam {
    private String loanApplyId;
    private String requestId;
    private String applyId;
    private String mobileNo;
    private long creditApplicationId;
    private Long quoteId;
    private String quoteNo;
    private long profileId;
    private long profileVersionId;
    private String status;
    private BigDecimal applyAmt;
    private String productCode;
    private String repayMethod;
    private Long couponId;
    private String loanPurpose;
    private BigDecimal lat;
    private BigDecimal lng;
    private String ip;
    private String address;
    private String adId;

    public static LoanApplicationInsertParam from(LoanApplicationInsert insert) {
        LoanApplicationInsertParam p = new LoanApplicationInsertParam();
        p.loanApplyId = insert.loanApplyId();
        p.requestId = insert.requestId();
        p.applyId = insert.applyId();
        p.mobileNo = insert.mobileNo();
        p.creditApplicationId = insert.creditApplicationId();
        p.quoteId = insert.quoteId();
        p.quoteNo = insert.quoteNo();
        p.profileId = insert.profileId();
        p.profileVersionId = insert.profileVersionId();
        p.status = insert.status();
        p.applyAmt = insert.applyAmt();
        p.productCode = insert.productCode();
        p.repayMethod = insert.repayMethod();
        p.couponId = insert.couponId();
        p.loanPurpose = insert.loanPurpose();
        p.lat = insert.lat();
        p.lng = insert.lng();
        p.ip = insert.ip();
        p.address = insert.address();
        p.adId = insert.adId();
        return p;
    }

    public String getLoanApplyId() {
        return loanApplyId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getApplyId() {
        return applyId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public long getCreditApplicationId() {
        return creditApplicationId;
    }

    public Long getQuoteId() {
        return quoteId;
    }

    public String getQuoteNo() {
        return quoteNo;
    }

    public long getProfileId() {
        return profileId;
    }

    public long getProfileVersionId() {
        return profileVersionId;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getApplyAmt() {
        return applyAmt;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getRepayMethod() {
        return repayMethod;
    }

    public Long getCouponId() {
        return couponId;
    }

    public String getLoanPurpose() {
        return loanPurpose;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public String getIp() {
        return ip;
    }

    public String getAddress() {
        return address;
    }

    public String getAdId() {
        return adId;
    }
}
