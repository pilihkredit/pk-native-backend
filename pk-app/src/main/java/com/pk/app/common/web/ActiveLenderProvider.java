package com.pk.app.common.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Active lender identity used to decorate every {@link ApiResponse}.
 */
@Component
public class ActiveLenderProvider {
    private final String providerCode;
    private final String providerName;

    public ActiveLenderProvider(
            @Value("${pk.lender.config.provider-code:apipartner}") String providerCode,
            @Value("${pk.lender.config.provider-name:}") String providerName
    ) {
        this.providerCode = blankToNull(providerCode);
        this.providerName = blankToNull(providerName);
    }

    public String providerCode() {
        return providerCode;
    }

    public String providerName() {
        return providerName;
    }

    public <T> ApiResponse<T> enrich(ApiResponse<T> response) {
        if (response == null) {
            return null;
        }
        if (response.lenderProvider() != null || response.lenderProviderName() != null) {
            return response;
        }
        return response.withLenderProvider(providerCode, providerName);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
