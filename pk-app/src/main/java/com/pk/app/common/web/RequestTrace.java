package com.pk.app.common.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public final class RequestTrace {
    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String TRACE_ATTR = "pk.resolvedTraceId";
    private static final String CLIENT_REQUEST_ATTR = "pk.resolvedClientRequestId";

    private RequestTrace() {
    }

    public static String resolveTraceId(HttpServletRequest request) {
        Object cached = request.getAttribute(TRACE_ATTR);
        if (cached instanceof String value && !value.isBlank()) {
            return value;
        }
        String header = trimToNull(request.getHeader(TRACE_HEADER));
        String traceId = header != null ? header : UUID.randomUUID().toString();
        request.setAttribute(TRACE_ATTR, traceId);
        return traceId;
    }

    /**
     * Prefer body requestId, then X-Request-Id, then the resolved trace id (always non-blank).
     */
    public static String resolveClientRequestId(HttpServletRequest request, String bodyRequestId) {
        String body = trimToNull(bodyRequestId);
        if (body != null) {
            request.setAttribute(CLIENT_REQUEST_ATTR, body);
            return body;
        }
        Object cached = request.getAttribute(CLIENT_REQUEST_ATTR);
        if (cached instanceof String value && !value.isBlank()) {
            return value;
        }
        String header = trimToNull(request.getHeader(REQUEST_ID_HEADER));
        String clientRequestId = header != null ? header : resolveTraceId(request);
        request.setAttribute(CLIENT_REQUEST_ATTR, clientRequestId);
        return clientRequestId;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
