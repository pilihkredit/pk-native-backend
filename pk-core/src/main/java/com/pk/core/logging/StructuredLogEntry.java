package com.pk.core.logging;

import java.util.Map;

public record StructuredLogEntry(
        String level,
        String message,
        String traceId,
        String uri,
        String method,
        Integer status,
        Long durationMs,
        String code,
        Map<String, ?> extra
) {
    public static Builder builder(String level, String message) {
        return new Builder(level, message);
    }

    public static final class Builder {
        private final String level;
        private final String message;
        private String traceId;
        private String uri;
        private String method;
        private Integer status;
        private Long durationMs;
        private String code;
        private Map<String, ?> extra;

        private Builder(String level, String message) {
            this.level = level;
            this.message = message;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder status(Integer status) {
            this.status = status;
            return this;
        }

        public Builder durationMs(Long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder extra(Map<String, ?> extra) {
            this.extra = extra;
            return this;
        }

        public StructuredLogEntry build() {
            return new StructuredLogEntry(level, message, traceId, uri, method, status, durationMs, code, extra);
        }
    }
}
