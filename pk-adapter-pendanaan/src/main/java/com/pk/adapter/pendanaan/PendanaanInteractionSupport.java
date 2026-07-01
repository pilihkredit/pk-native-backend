package com.pk.adapter.pendanaan;

import com.pk.core.external.LenderInteractionLog;
import com.pk.core.external.port.LenderInteractionLogRepository;
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
                    responseMsg,
                    success,
                    durationMs,
                    PendanaanHttpSupport.formatLogBody(redactedResponse, logging.maxBodyBytes())
            );
        }
        repository.insert(new LenderInteractionLog(
                PROVIDER_CODE,
                interactionNo,
                businessType,
                businessId,
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
}
