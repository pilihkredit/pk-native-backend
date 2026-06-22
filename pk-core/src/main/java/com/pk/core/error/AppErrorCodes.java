package com.pk.core.error;

public final class AppErrorCodes {
    public static final String SUCCESS = "000000";
    public static final String INVALID_REQUEST = "K000001";
    public static final String UNAUTHORIZED = "K000012";
    public static final String DEVICE_NO_REQUIRED = "K000017";
    public static final String INVALID_MOBILE = "K000090";
    public static final String TOO_MANY_REQUESTS = "K000201";
    public static final String INVALID_OTP = "K000202";
    public static final String OTP_TOKEN_MISMATCH = "K000206";
    public static final String PROFILE_SYNC_FAILED = "B000001";
    public static final String QUOTE_EXPIRED = "B000003";
    public static final String SERVICE_UNAVAILABLE = "999998";
    public static final String INTERNAL_ERROR = "999999";

    private AppErrorCodes() {
    }
}
