package com.pk.app.common.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

public final class RequestTrace {
    private static final String TRACE_HEADER = "X-Trace-Id";

    private RequestTrace() {
    }

    public static String resolveTraceId(HttpServletRequest request) {
        String header = request.getHeader(TRACE_HEADER);
        if (header != null && !header.isBlank()) {
            return header;
        }
        return UUID.randomUUID().toString();
    }
}
