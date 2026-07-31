package com.pk.infra.ocr.mapper;

import java.math.BigDecimal;

public class OcrVendorCallLogInsertParam {
    private Long id;
    private Long userId;
    private String partnerUserId;
    private String mobileNo;
    private String operationType;
    private String channel;
    private String traceId;
    private String clientRequestId;
    private String status;
    private String apiCode;
    private String vendorCode;
    private String vendorMessage;
    private BigDecimal score;
    private BigDecimal threshold;
    private String endpoint;
    private Integer httpStatus;
    private Integer durationMs;
    private String requestJson;
    private String responseJson;
    private String idCardImageEncryptedRef;
    private String livenessImageEncryptedRef;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPartnerUserId() {
        return partnerUserId;
    }

    public void setPartnerUserId(String partnerUserId) {
        this.partnerUserId = partnerUserId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getVendorMessage() {
        return vendorMessage;
    }

    public void setVendorMessage(String vendorMessage) {
        this.vendorMessage = vendorMessage;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Integer getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public String getRequestJson() {
        return requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public String getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }

    public String getIdCardImageEncryptedRef() {
        return idCardImageEncryptedRef;
    }

    public void setIdCardImageEncryptedRef(String idCardImageEncryptedRef) {
        this.idCardImageEncryptedRef = idCardImageEncryptedRef;
    }

    public String getLivenessImageEncryptedRef() {
        return livenessImageEncryptedRef;
    }

    public void setLivenessImageEncryptedRef(String livenessImageEncryptedRef) {
        this.livenessImageEncryptedRef = livenessImageEncryptedRef;
    }
}
