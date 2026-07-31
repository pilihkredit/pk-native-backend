package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;

public final class TrustDecisionLenderRawOcrDetailBuilder {
    private TrustDecisionLenderRawOcrDetailBuilder() {
    }

    public static String build(ObjectMapper objectMapper, String rawJson) {
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(rawJson));
        } catch (Exception exception) {
            throw new ApiException(ApiCode.OCR_NO_RESULT);
        }
    }
}
