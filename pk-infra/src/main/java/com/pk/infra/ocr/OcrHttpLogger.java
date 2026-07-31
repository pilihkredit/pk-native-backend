package com.pk.infra.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OcrHttpLogger {
    private static final Logger log = LoggerFactory.getLogger(OcrHttpLogger.class);

    private OcrHttpLogger() {
    }

    public static void logRequest(String operation, String method, String endpoint, String requestBody) {
        log.info(
                "OCR request op={} method={} endpoint={} body={}",
                operation,
                method,
                endpoint,
                requestBody == null ? "" : requestBody
        );
    }

    public static void logResponse(
            String operation,
            String endpoint,
            long durationMs,
            boolean success,
            String responseBody
    ) {
        log.info(
                "OCR response op={} endpoint={} durationMs={} success={} body={}",
                operation,
                endpoint,
                durationMs,
                success,
                responseBody == null ? "" : responseBody
        );
    }
}
