package com.pk.infra.provider;

record LenderProviderConfigRecord(
        String providerCode,
        String baseUrl,
        String callbackBaseUrl,
        String configJson,
        String clientId,
        String clientSecret,
        String callbackClientId,
        String callbackClientSecret
) {
}
