package com.pk.app.web;

import com.pk.core.error.AppBusinessException;
import com.pk.core.error.AppErrorCodes;
import jakarta.servlet.http.HttpServletRequest;

public final class RequestSupport {
    public static final String TRACE_ID_ATTRIBUTE = "traceId";

    private RequestSupport() {
    }

    public static String traceId(HttpServletRequest request) {
        Object attribute = request.getAttribute(TRACE_ID_ATTRIBUTE);
        if (attribute instanceof String traceId && !traceId.isBlank()) {
            return traceId;
        }
        String header = request.getHeader("X-Trace-Id");
        if (header != null && !header.isBlank()) {
            return header;
        }
        return java.util.UUID.randomUUID().toString();
    }

    public static String requireDeviceHeader(HttpServletRequest request) {
        String deviceNo = request.getHeader("X-Device-No");
        if (deviceNo == null || deviceNo.isBlank()) {
            throw new AppBusinessException(AppErrorCodes.DEVICE_NO_REQUIRED);
        }
        return deviceNo;
    }

    public static void assertDeviceMatches(HttpServletRequest request, String bodyDeviceNo) {
        String headerDeviceNo = requireDeviceHeader(request);
        if (bodyDeviceNo == null || bodyDeviceNo.isBlank() || !headerDeviceNo.equals(bodyDeviceNo)) {
            throw new AppBusinessException(AppErrorCodes.INVALID_REQUEST);
        }
    }
}
