package com.pk.core.logging;

import org.slf4j.MDC;

public final class LogContext {
    public static final String TRACE_ID = "traceId";
    public static final String SERVICE = "service";
    public static final String ENVIRONMENT = "environment";
    public static final String MOBILE_NO = "mobileNo";

    private LogContext() {
    }

    public static void putTraceId(String traceId) {
        if (traceId != null && !traceId.isBlank()) {
            MDC.put(TRACE_ID, traceId);
        }
    }

    public static void putService(String service) {
        if (service != null && !service.isBlank()) {
            MDC.put(SERVICE, service);
        }
    }

    public static void putEnvironment(String environment) {
        if (environment != null && !environment.isBlank()) {
            MDC.put(ENVIRONMENT, environment);
        }
    }

    public static void putMobileNo(String mobileNo) {
        if (mobileNo != null && !mobileNo.isBlank()) {
            MDC.put(MOBILE_NO, mobileNo.trim());
        }
    }

    public static String traceId() {
        return MDC.get(TRACE_ID);
    }

    public static String mobileNo() {
        return MDC.get(MOBILE_NO);
    }

    public static void clear() {
        MDC.clear();
    }
}
