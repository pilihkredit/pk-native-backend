package com.pk.adapter.apipartner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.core.logging.PlatformStructuredLogger;
import org.springframework.lang.Nullable;

final class ApiPartnerHttpStack {
    @Nullable
    private final ApiPartnerHttpClient httpClient;

    private ApiPartnerHttpStack(@Nullable ApiPartnerHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    static ApiPartnerHttpStack create(
            ApiPartnerProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper,
            @Nullable PlatformStructuredLogger structuredLogger
    ) {
        if (!properties.httpEnabled() || !properties.httpCredentialsPresent()) {
            return new ApiPartnerHttpStack(null);
        }
        ApiPartnerOAuthTokenProvider tokenProvider = new ApiPartnerOAuthTokenProvider(
                properties,
                interactionLogRepository,
                objectMapper,
                structuredLogger
        );
        return new ApiPartnerHttpStack(new ApiPartnerHttpClient(
                properties,
                tokenProvider,
                interactionLogRepository,
                objectMapper,
                structuredLogger
        ));
    }

    boolean enabled() {
        return httpClient != null;
    }

    ApiPartnerHttpClient requireHttpClient() {
        if (httpClient == null) {
            throw new IllegalStateException("ApiPartner HTTP client is not configured");
        }
        return httpClient;
    }
}
