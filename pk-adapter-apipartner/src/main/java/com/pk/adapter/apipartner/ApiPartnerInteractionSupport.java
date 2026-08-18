package com.pk.adapter.apipartner;

import com.pk.core.external.LenderInteractionLog;
import com.pk.core.external.LenderInteractionContext;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.logging.PlatformStructuredLogger;
import com.pk.core.logging.StructuredLogEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ApiPartnerInteractionSupport {
    static final String PROVIDER_CODE = "apipartner";

    private static final Logger log = LoggerFactory.getLogger(ApiPartnerInteractionSupport.class);

    private ApiPartnerInteractionSupport() {
    }

    static long log(
            LenderInteractionLogRepository repository,
            ApiPartnerProperties.Logging logging,
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
        String redactedRequest = ApiPartnerHttpSupport.redactSensitiveJson(requestBody);
        String redactedResponse = ApiPartnerHttpSupport.redactSensitiveJson(responseText);
        int requestBytes = ApiPartnerHttpSupport.requestBodyBytes(requestBody);
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
                    ApiPartnerHttpSupport.formatLogBody(redactedRequest, logging.maxBodyBytes())
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
                    ApiPartnerHttpSupport.formatLogBody(redactedResponse, logging.maxBodyBytes())
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
                extra.put("requestBody", ApiPartnerHttpSupport.formatLogBody(redactedRequest, logging.maxBodyBytes()));
                extra.put("responseBody", ApiPartnerHttpSupport.formatLogBody(redactedResponse, logging.maxBodyBytes()));
            }
            structuredLogger.log(StructuredLogEntry.builder("ERROR", "ApiPartner")
                    .uri(endpoint)
                    .method(method)
                    .status(httpStatus)
                    .durationMs((long) durationMs)
                    .code(responseCode)
                    .extra(extra)
                    .build());
        }
        return repository.insert(new LenderInteractionLog(
                PROVIDER_CODE,
                interactionNo,
                businessType,
                businessId,
                resolveMobileNo(requestBody),
                method,
                endpoint,
                interactionNo,
                ApiPartnerHttpSupport.sha256Hex(requestBody == null ? "" : requestBody),
                ApiPartnerHttpSupport.truncate(redactedRequest),
                responseCode,
                responseMsg,
                ApiPartnerHttpSupport.truncate(redactedResponse),
                success,
                durationMs,
                LenderInteractionContext.source()
        ));
    }

    private static String resolveMobileNo(String requestBody) {
        String fromContext = LenderInteractionContext.mobileNo();
        if (fromContext != null && !fromContext.isBlank()) {
            return fromContext.trim();
        }
        return ApiPartnerHttpSupport.extractMobileNo(requestBody);
    }
}
