package com.pk.adapter.pendanaan;

import com.pk.core.external.LenderInteractionLog;
import com.pk.core.external.LenderInteractionContext;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.logging.PlatformStructuredLogger;
import com.pk.core.logging.StructuredLogEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class PendanaanInteractionSupport {
    static final String PROVIDER_CODE = "pendanaan";

    private static final Logger log = LoggerFactory.getLogger(PendanaanInteractionSupport.class);

    private PendanaanInteractionSupport() {
    }

    static void log(
            LenderInteractionLogRepository repository,
            PendanaanProperties.Logging logging,
            PlatformStructuredLogger structuredLogger,
            String interactionNo,
            String businessType,
            String businessId,
            String method,
            String endpoint,
            String requestBody,
            String responseCode,
            String responseMsg,
            String responseText,
            boolean success,
            int durationMs,
            Integer httpStatus
    ) {
        String redactedRequest = PendanaanHttpSupport.redactSensitiveJson(requestBody);
        String redactedResponse = PendanaanHttpSupport.redactSensitiveJson(responseText);
        int requestBytes = PendanaanHttpSupport.requestBodyBytes(requestBody);
        if (logging != null && logging.enabled()) {
            log.info(
                    "Lender request provider={} interactionNo={} businessType={} businessId={} method={} endpoint={} requestBytes={} body={}",
                    PROVIDER_CODE,
                    interactionNo,
                    businessType,
                    businessId,
                    method,
                    endpoint,
                    requestBytes,
                    PendanaanHttpSupport.formatLogBody(redactedRequest, logging.maxBodyBytes())
            );
            log.info(
                    "Lender response provider={} interactionNo={} businessType={} businessId={} method={} endpoint={} httpStatus={} code={} msg={} success={} durationMs={} body={}",
                    PROVIDER_CODE,
                    interactionNo,
                    businessType,
                    businessId,
                    method,
                    endpoint,
                    httpStatus == null ? "" : httpStatus,
                    responseCode,
                    responseMsg == null ? "" : responseMsg,
                    success,
                    durationMs,
                    PendanaanHttpSupport.formatLogBody(redactedResponse, logging.maxBodyBytes())
            );
        }
        if (structuredLogger != null && !success) {
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("provider", PROVIDER_CODE);
            extra.put("interactionNo", interactionNo);
            extra.put("businessType", businessType);
            extra.put("businessId", businessId);
            extra.put("responseMsg", responseMsg);
            if (logging != null && logging.enabled()) {
                extra.put("requestBody", PendanaanHttpSupport.formatLogBody(redactedRequest, logging.maxBodyBytes()));
                extra.put("responseBody", PendanaanHttpSupport.formatLogBody(redactedResponse, logging.maxBodyBytes()));
            }
            structuredLogger.log(StructuredLogEntry.builder("ERROR", "Pendanaan")
                    .uri(endpoint)
                    .method(method)
                    .status(httpStatus)
                    .durationMs((long) durationMs)
                    .code(responseCode)
                    .extra(extra)
                    .build());
        }
        repository.insert(new LenderInteractionLog(
                PROVIDER_CODE,
                interactionNo,
                businessType,
                businessId,
                resolveMobileNo(requestBody),
                method,
                endpoint,
                interactionNo,
                PendanaanHttpSupport.sha256Hex(requestBody == null ? "" : requestBody),
                PendanaanHttpSupport.truncate(redactedRequest),
                responseCode,
                responseMsg,
                PendanaanHttpSupport.truncate(redactedResponse),
                success,
                durationMs
        ));
    }

    private static String resolveMobileNo(String requestBody) {
        String fromContext = LenderInteractionContext.mobileNo();
        if (fromContext != null && !fromContext.isBlank()) {
            return fromContext.trim();
        }
        return PendanaanHttpSupport.extractMobileNo(requestBody);
    }
}
