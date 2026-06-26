package com.pk.core.external;

public record LenderInteractionLog(
        String providerCode,
        String interactionNo,
        String businessType,
        String businessId,
        String httpMethod,
        String endpoint,
        String requestId,
        String requestHash,
        String requestRef,
        String responseCode,
        String responseMsg,
        String responseRef,
        boolean success,
        int durationMs
) {
}
