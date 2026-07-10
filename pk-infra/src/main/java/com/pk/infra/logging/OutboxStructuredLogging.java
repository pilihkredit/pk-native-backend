package com.pk.infra.logging;

import com.pk.core.logging.PlatformStructuredLogger;
import com.pk.core.logging.StructuredLogEntry;
import com.pk.core.outbox.OutboxEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OutboxStructuredLogging {
    private OutboxStructuredLogging() {
    }

    public static void logTerminalFailure(
            PlatformStructuredLogger structuredLogger,
            OutboxEvent event,
            int attempts,
            Throwable throwable
    ) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("eventNo", event.eventNo());
        extra.put("eventType", event.eventType());
        extra.put("attempts", attempts);
        if (throwable != null) {
            extra.put("error", throwable.getClass().getSimpleName());
            extra.put("errorMessage", throwable.getMessage());
        }
        structuredLogger.log(StructuredLogEntry.builder("ERROR", "outbox")
                .extra(extra)
                .build());
    }

    public static void logRetry(
            PlatformStructuredLogger structuredLogger,
            OutboxEvent event,
            int nextRetry,
            Throwable throwable
    ) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("eventNo", event.eventNo());
        extra.put("eventType", event.eventType());
        extra.put("nextRetry", nextRetry);
        if (throwable != null) {
            extra.put("error", throwable.getClass().getSimpleName());
            extra.put("errorMessage", throwable.getMessage());
        }
        structuredLogger.log(StructuredLogEntry.builder("WARN", "outbox")
                .extra(extra)
                .build());
    }
}
