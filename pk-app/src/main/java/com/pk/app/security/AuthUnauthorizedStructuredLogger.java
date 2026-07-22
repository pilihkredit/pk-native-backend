package com.pk.app.security;

import com.pk.app.common.web.RequestTrace;
import com.pk.core.logging.StructuredLogEntry;
import com.pk.infra.auth.AuthRejectReasons;
import com.pk.infra.logging.StructuredLogWriter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AuthUnauthorizedStructuredLogger {
    private final StructuredLogWriter structuredLogWriter;

    public AuthUnauthorizedStructuredLogger(StructuredLogWriter structuredLogWriter) {
        this.structuredLogWriter = structuredLogWriter;
    }

    public void log(HttpServletRequest request, String reason) {
        String resolvedReason = reason == null || reason.isBlank()
                ? AuthRejectReasons.TOKEN_REJECTED
                : reason;
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("reason", resolvedReason);
        structuredLogWriter.log(StructuredLogEntry.builder("WARN", "auth.unauthorized")
                .traceId(RequestTrace.resolveTraceId(request))
                .uri(request.getRequestURI())
                .method(request.getMethod())
                .status(401)
                .code("K000012")
                .extra(extra)
                .build());
    }
}
