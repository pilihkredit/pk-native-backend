package com.pk.app.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);

    private final ApiProperties apiProperties;
    private final ApiLoggingProperties loggingProperties;

    public ApiLoggingFilter(ApiProperties apiProperties, ApiLoggingProperties loggingProperties) {
        this.apiProperties = apiProperties;
        this.loggingProperties = loggingProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !loggingProperties.enabled() || !matchesApiPath(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        String traceId = RequestTrace.resolveTraceId(request);
        long startedAt = System.currentTimeMillis();
        String method = request.getMethod();
        String path = request.getRequestURI();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            boolean redactOcrFields = isOcrPath(path);
            log.info(
                    "API request method={} path={} traceId={} query={} body={}",
                    method,
                    path,
                    traceId,
                    ApiHttpPayloadFormatter.formatQueryString(request),
                    ApiHttpPayloadFormatter.formatBody(
                            requestWrapper.getContentAsByteArray(),
                            request.getContentType(),
                            loggingProperties.maxBodyBytes(),
                            redactOcrFields
                    )
            );
            log.info(
                    "API response method={} path={} traceId={} status={} durationMs={} body={}",
                    method,
                    path,
                    traceId,
                    responseWrapper.getStatus(),
                    durationMs,
                    ApiHttpPayloadFormatter.formatBody(
                            responseWrapper.getContentAsByteArray(),
                            responseWrapper.getContentType(),
                            loggingProperties.maxBodyBytes(),
                            redactOcrFields
                    )
            );
            responseWrapper.copyBodyToResponse();
        }
    }

    private boolean matchesApiPath(HttpServletRequest request) {
        String prefix = normalizePrefix(apiProperties.v1Prefix());
        String path = request.getRequestURI();
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return ApiPaths.V1_PREFIX;
        }
        return prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
    }

    private static boolean isOcrPath(String path) {
        return path != null && path.contains("/profile/identity/ocr");
    }
}
