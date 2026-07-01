package com.pk.adapter.pendanaan;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.external.port.LenderInteractionLogRepository;
import org.springframework.lang.Nullable;

final class PendanaanHttpStack {
    @Nullable
    private final PendanaanHttpClient httpClient;

    private PendanaanHttpStack(@Nullable PendanaanHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    static PendanaanHttpStack create(
            PendanaanProperties properties,
            LenderInteractionLogRepository interactionLogRepository,
            ObjectMapper objectMapper
    ) {
        if (!properties.httpEnabled() || !properties.httpCredentialsPresent()) {
            return new PendanaanHttpStack(null);
        }
        PendanaanOAuthTokenProvider tokenProvider = new PendanaanOAuthTokenProvider(
                properties,
                interactionLogRepository,
                objectMapper
        );
        return new PendanaanHttpStack(new PendanaanHttpClient(
                properties,
                tokenProvider,
                interactionLogRepository,
                objectMapper
        ));
    }

    boolean enabled() {
        return httpClient != null;
    }

    PendanaanHttpClient requireHttpClient() {
        if (httpClient == null) {
            throw new IllegalStateException("Pendanaan HTTP client is not configured");
        }
        return httpClient;
    }
}
