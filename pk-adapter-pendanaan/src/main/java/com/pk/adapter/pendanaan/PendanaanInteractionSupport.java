package com.pk.adapter.pendanaan;

import com.pk.core.external.LenderInteractionLog;
import com.pk.core.external.port.LenderInteractionLogRepository;

final class PendanaanInteractionSupport {
    static final String PROVIDER_CODE = "pendanaan";

    private PendanaanInteractionSupport() {
    }

    static void log(
            LenderInteractionLogRepository repository,
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
            int durationMs
    ) {
        repository.insert(new LenderInteractionLog(
                PROVIDER_CODE,
                interactionNo,
                businessType,
                businessId,
                method,
                endpoint,
                interactionNo,
                PendanaanHttpSupport.sha256Hex(requestBody),
                PendanaanHttpSupport.truncate(requestBody),
                responseCode,
                responseMsg,
                PendanaanHttpSupport.truncate(responseText),
                success,
                durationMs
        ));
    }
}
