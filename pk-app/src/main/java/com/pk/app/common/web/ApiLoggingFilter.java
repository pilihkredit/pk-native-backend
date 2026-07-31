package com.pk.app.common.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.logging.StructuredLogEntry;
import com.pk.infra.logging.StructuredLogWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(1)
public class ApiLoggingFilter extends OncePerRequestFilter {
    private final ApiProperties apiProperties;
    private final ApiLoggingProperties loggingProperties;
    private final StructuredLogWriter structuredLogWriter;
    private final ObjectMapper objectMapper;

    public ApiLoggingFilter(
            ApiProperties apiProperties,
            ApiLoggingProperties loggingProperties,
            StructuredLogWriter structuredLogWriter,
            ObjectMapper objectMapper
    ) {
        this.apiProperties = apiProperties;
        this.loggingProperties = loggingProperties;
        this.structuredLogWriter = structuredLogWriter;
        this.objectMapper = objectMapper;
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
            String requestBody = ApiHttpPayloadFormatter.formatBody(
                    requestWrapper.getContentAsByteArray(),
                    request.getContentType(),
                    loggingProperties.maxBodyBytes(),
                    redactOcrFields
            );
            byte[] responseBytes = responseWrapper.getContentAsByteArray();
            String responseBody = ApiHttpPayloadFormatter.formatBody(
                    responseBytes,
                    responseWrapper.getContentType(),
                    loggingProperties.maxBodyBytes(),
                    redactOcrFields
            );
            Map<String, Object> extra = new LinkedHashMap<>();
            Map<String, String> requestHeaders = ApiHttpPayloadFormatter.formatHeaders(request);
            if (!requestHeaders.isEmpty()) {
                extra.put("requestHeaders", requestHeaders);
            }
            String query = ApiHttpPayloadFormatter.formatQueryString(request);
            if (!query.isBlank()) {
                extra.put("query", query);
            }
            if (!requestBody.isBlank()) {
                extra.put("requestBody", requestBody);
            }
            if (!responseBody.isBlank()) {
                extra.put("responseBody", responseBody);
            }
            structuredLogWriter.log(StructuredLogEntry.builder(resolveLevel(responseWrapper.getStatus()), "api.access")
                    .traceId(traceId)
                    .uri(path)
                    .method(method)
                    .status(responseWrapper.getStatus())
                    .durationMs(durationMs)
                    .code(extractBusinessCode(responseBytes))
                    .extra(extra.isEmpty() ? null : extra)
                    .build());
            responseWrapper.copyBodyToResponse();
        }
    }

    private String resolveLevel(int status) {
        if (status >= 500) {
            return "ERROR";
        }
        if (status >= 400) {
            return "WARN";
        }
        return "INFO";
    }

    private String extractBusinessCode(byte[] responseBytes) {
        if (responseBytes == null || responseBytes.length == 0) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBytes);
            JsonNode code = root.get("code");
            if (code == null || code.isNull()) {
                return null;
            }
            String value = code.asText();
            return value.isBlank() ? null : value;
        } catch (Exception ignored) {
            return null;
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
