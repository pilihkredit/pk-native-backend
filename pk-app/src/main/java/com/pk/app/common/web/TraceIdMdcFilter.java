package com.pk.app.common.web;

import com.pk.core.logging.LogContext;
import com.pk.infra.logging.LoggingRuntimeContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdMdcFilter extends OncePerRequestFilter {
    private final LoggingRuntimeContext runtimeContext;

    public TraceIdMdcFilter(LoggingRuntimeContext runtimeContext) {
        this.runtimeContext = runtimeContext;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String traceId = RequestTrace.resolveTraceId(request);
        LogContext.putTraceId(traceId);
        LogContext.putService(runtimeContext.service());
        LogContext.putEnvironment(runtimeContext.environment());
        try {
            filterChain.doFilter(request, response);
        } finally {
            LogContext.clear();
        }
    }
}
