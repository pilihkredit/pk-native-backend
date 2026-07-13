package com.pk.infra.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.logging.LogContext;
import com.pk.core.logging.PlatformStructuredLogger;
import com.pk.core.logging.StructuredLogEntry;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StructuredLogWriter implements PlatformStructuredLogger {
    private static final Logger STRUCTURED_LOG = LoggerFactory.getLogger("pk.structured");
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final ObjectMapper objectMapper;
    private final LoggingRuntimeContext runtimeContext;

    public StructuredLogWriter(ObjectMapper objectMapper, LoggingRuntimeContext runtimeContext) {
        this.objectMapper = objectMapper;
        this.runtimeContext = runtimeContext;
    }

    @Override
    public void log(StructuredLogEntry entry) {
        try {
            String json = buildJson(entry);
            logByLevel(entry.level(), json);
        } catch (Exception exception) {
            LoggerFactory.getLogger(StructuredLogWriter.class)
                    .warn("Failed to write structured log message={}", entry.message(), exception);
        }
    }

    private String buildJson(StructuredLogEntry entry) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("timestamp", TIMESTAMP_FORMATTER.format(Instant.now()));
        root.put("level", entry.level());
        root.put("service", runtimeContext.service());
        root.put("environment", runtimeContext.environment());
        String traceId = firstNonBlank(entry.traceId(), LogContext.traceId());
        if (traceId != null) {
            root.put("traceId", traceId);
        }
        root.put("message", entry.message());
        putIfPresent(root, "uri", entry.uri());
        putIfPresent(root, "method", entry.method());
        if (entry.status() != null) {
            root.put("status", entry.status());
        }
        if (entry.durationMs() != null) {
            root.put("durationMs", entry.durationMs());
        }
        putIfPresent(root, "code", entry.code());
        putIfPresent(root, "mobileNo", LogContext.mobileNo());
        if (entry.extra() != null && !entry.extra().isEmpty()) {
            root.set("extra", objectMapper.valueToTree(entry.extra()));
        }
        return objectMapper.writeValueAsString(root);
    }

    private static void logByLevel(String level, String json) {
        switch (level == null ? "INFO" : level.toUpperCase()) {
            case "ERROR" -> STRUCTURED_LOG.error(json);
            case "WARN" -> STRUCTURED_LOG.warn(json);
            case "DEBUG" -> STRUCTURED_LOG.debug(json);
            default -> STRUCTURED_LOG.info(json);
        }
    }

    private static void putIfPresent(ObjectNode root, String field, String value) {
        if (value != null && !value.isBlank()) {
            root.put(field, value);
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    public void logError(String message, String traceId, String uri, String method, Map<String, ?> extra, Throwable throwable) {
        Map<String, Object> enriched = new java.util.LinkedHashMap<>();
        if (extra != null) {
            enriched.putAll(extra);
        }
        if (throwable != null) {
            enriched.put("error", throwable.getClass().getSimpleName());
            enriched.put("errorMessage", throwable.getMessage());
            enriched.put("stackTrace", stackTrace(throwable));
        }
        log(StructuredLogEntry.builder("ERROR", message)
                .traceId(traceId)
                .uri(uri)
                .method(method)
                .extra(enriched.isEmpty() ? null : enriched)
                .build());
    }

    private static String stackTrace(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        builder.append(throwable).append('\n');
        for (StackTraceElement element : throwable.getStackTrace()) {
            builder.append("  at ").append(element).append('\n');
            if (builder.length() > 8_000) {
                builder.append("  ...[truncated]");
                break;
            }
        }
        return builder.toString();
    }
}
