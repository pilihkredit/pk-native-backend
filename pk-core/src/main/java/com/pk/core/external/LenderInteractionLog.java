package com.pk.core.external;

public class LenderInteractionLog {
    private Long id;
    private final String providerCode;
    private final String interactionNo;
    private final String businessType;
    private final String businessId;
    private final String mobileNo;
    private final String httpMethod;
    private final String endpoint;
    private final String requestId;
    private final String requestHash;
    private final String requestRef;
    private final String responseCode;
    private final String responseMsg;
    private final String responseRef;
    private final boolean success;
    private final int durationMs;
    private final String source;

    public LenderInteractionLog(
            String providerCode,
            String interactionNo,
            String businessType,
            String businessId,
            String mobileNo,
            String httpMethod,
            String endpoint,
            String requestId,
            String requestHash,
            String requestRef,
            String responseCode,
            String responseMsg,
            String responseRef,
            boolean success,
            int durationMs,
            String source
    ) {
        this.providerCode = providerCode;
        this.interactionNo = interactionNo;
        this.businessType = businessType;
        this.businessId = businessId;
        this.mobileNo = mobileNo;
        this.httpMethod = httpMethod;
        this.endpoint = endpoint;
        this.requestId = requestId;
        this.requestHash = requestHash;
        this.requestRef = requestRef;
        this.responseCode = responseCode;
        this.responseMsg = responseMsg;
        this.responseRef = responseRef;
        this.success = success;
        this.durationMs = durationMs;
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public String getInteractionNo() {
        return interactionNo;
    }

    public String getBusinessType() {
        return businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public String getRequestRef() {
        return requestRef;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public String getResponseRef() {
        return responseRef;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public String getSource() {
        return source;
    }
}
